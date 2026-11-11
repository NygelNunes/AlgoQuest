(function () {
  "use strict";
  const { SIMS } = window.AQSims;
  const q = (id) => document.getElementById(id);
  const consulta = new URLSearchParams(location.search);
  if (consulta.get("embed") === "1") document.body.classList.add("lab-embed");

  const NOMES = ["Sem treino", "Noviço", "Adepto", "Mestre"];
  const chaveM = () => "aq_maestria_" + ((Sessao.obter() || {}).nomeUsuario || "visitante");
  const lerM = () => { try { return JSON.parse(localStorage.getItem(chaveM())) || {}; } catch (e) { return {}; } };
  const gravarM = (m) => { try { localStorage.setItem(chaveM(), JSON.stringify(m)); } catch (e) {  } };
  const nivelM = (s) => (!s || !s.t ? 0 : s.t >= 10 && s.a / s.t >= 0.8 ? 3 : s.t >= 5 && s.a / s.t >= 0.6 ? 2 : 1);

  let simId = SIMS[consulta.get("sim")] ? consulta.get("sim") : "bubble";
  let passos = [], i = 0, tocando = false, temporizador = null, desafio = false;
  let pendente = null, respondidos = new Map(), sessao = { a: 0, t: 0, seq: 0 };
  let manual = null;

  const R = {};

  R.bubble = {
    montar(palco, e) {
      const max = Math.max(...e.valores);
      palco.innerHTML = `<div class="barras" style="--n:${e.valores.length}">${e.valores.map((v, id) =>
        `<div class="barra" data-id="${id}" style="--i:${id}"><span class="barra__col" style="height:${Math.max(8, (v / max) * 100)}%"><b>${v}</b></span></div>`).join("")}</div>`;
    },
    desenhar(palco, p) {
      p.a.forEach((x, pos) => {
        const el = palco.querySelector(`.barra[data-id="${x.id}"]`);
        if (!el) return;
        el.style.setProperty("--i", pos);
        el.className = "barra" + (p.cmp && p.cmp.includes(pos) ? (p.troca ? " barra--troca" : " barra--cmp") : "") + (pos >= p.ord ? " barra--ok" : "");
      });
    },
  };

  R.busca = {
    montar() {},
    desenhar(palco, p) {
      const n = p.a.length;
      palco.innerHTML = `<div class="celulas" style="--n:${n}">${p.a.map((v, k) => {
        const fora = k < p.lo || k > p.hi, cls = (fora ? " cel--fora" : "") + (k === p.mid ? " cel--meio" : "") + (p.achou === k ? " cel--achou" : "");
        const marca = [k === p.lo && p.lo <= p.hi ? "início" : "", k === p.hi && p.lo <= p.hi ? "fim" : "", k === p.mid ? "meio" : ""].filter(Boolean).join(" · ");
        return `<div class="cel${cls}"><span class="cel__marca">${marca}</span><span class="cel__v">${v}</span><span class="cel__i">${k}</span></div>`;
      }).join("")}</div>`;
    },
  };

  R.hash = {
    montar() {},
    desenhar(palco, p) {
      palco.innerHTML = `<div class="hashtab">${p.tab.map((cadeia, k) => `<div class="hl${p.pos === k ? " hl--alvo" : ""}${p.pos === k && p.colisao ? " hl--colisao" : ""}">
        <span class="hl__i">${k}</span>
        <span class="hl__cadeia">${cadeia.length ? cadeia.map((c, j) => `${j ? '<i class="seta" aria-hidden="true">→</i>' : ""}<span class="chave${p.pos === k && c === p.chave ? " chave--nova" : ""}">${c}</span>`).join("") : '<span class="vazio">vazio</span>'}</span></div>`).join("")}</div>
        ${p.chave != null && p.pos == null ? `<p class="hashtab__chave">Chave em jogo: <b>${p.chave}</b></p>` : ""}`;
    },
  };

  const arvore = {
    layout(t) {
      const pos = {}; let x = 0, prof = 0;
      (function f(n, d) { if (!n) return; f(n.l, d + 1); pos[n.v] = { x: 40 + x++ * 46, y: 34 + d * 66 }; prof = Math.max(prof, d); f(n.r, d + 1); })(t, 0);
      return { pos, w: Math.max(320, 80 + Math.max(0, x - 1) * 46), h: 70 + prof * 66 };
    },
    quadro(palco, t, pos, p, mostraFb) {
      const cam = new Set(p.caminho || []), fb = window.AQSims.utils.fb;
      let arestas = "", nos = "";
      (function percorrer(n) {
        if (!n) return;
        [n.l, n.r].forEach((c) => { if (c && pos[n.v] && pos[c.v]) arestas += `<line class="ar${cam.has(n.v) && cam.has(c.v) ? " ar--cam" : ""}" x1="${pos[n.v].x}" y1="${pos[n.v].y}" x2="${pos[c.v].x}" y2="${pos[c.v].y}"/>`; });
        const o = pos[n.v];
        if (o) {
          const cls = (p.atual === n.v ? " no--atual" : "") + (cam.has(n.v) ? " no--cam" : "") + (p.novo === n.v ? " no--novo" : "") + (p.ruim === n.v ? " no--ruim" : "") + (p.girou === n.v ? " no--girou" : "");
          const fator = fb(n), fbTxt = mostraFb ? `<text class="fb${Math.abs(fator) > 1 ? " fb--mau" : ""}" x="${o.x + 17}" y="${o.y - 14}">${fator > 0 ? "+" : ""}${fator}</text>` : "";
          nos += `<g class="noarv${cls}"><circle cx="${o.x}" cy="${o.y}" r="17"/><text x="${o.x}" y="${o.y + 5}" text-anchor="middle">${n.v}</text>${fbTxt}</g>`;
        }
        percorrer(n.l); percorrer(n.r);
      })(t);
      palco.querySelector("svg").innerHTML = arestas + nos;
    },
    desenhar(palco, p, mostraFb) {
      const t = p.arvore, { pos, w, h } = arvore.layout(t);
      if (!palco.querySelector("svg.arv")) palco.innerHTML = `<svg class="arv" role="img" aria-label="Árvore da simulação" preserveAspectRatio="xMidYMid meet"></svg>`;
      const svg = palco.querySelector("svg");
      svg.setAttribute("viewBox", `0 0 ${w} ${h}`);
      svg.style.maxHeight = Math.max(200, h * 1.1) + "px";
      cancelAnimationFrame(palco._raf);
      const de = palco._pos || {};
      const mover = Object.keys(pos).some((k) => de[k] && (de[k].x !== pos[k].x || de[k].y !== pos[k].y));
      const quadroEm = (k) => { const cur = {}; Object.keys(pos).forEach((v) => { const a = de[v] || pos[v], b = pos[v]; cur[v] = { x: a.x + (b.x - a.x) * k, y: a.y + (b.y - a.y) * k }; }); arvore.quadro(palco, t, cur, p, mostraFb); };
      if (!mover || SEM_MOVIMENTO) { quadroEm(1); }
      else {
        const t0 = performance.now(), dur = 520;
        const passo = (agora) => { const k = Math.min(1, (agora - t0) / dur), e = 1 - Math.pow(1 - k, 3); quadroEm(e); if (k < 1) palco._raf = requestAnimationFrame(passo); };
        palco._raf = requestAnimationFrame(passo);
      }
      palco._pos = pos;
    },
  };
  R.bst = { montar(palco) { palco._pos = null; }, desenhar: (palco, p) => arvore.desenhar(palco, p, false) };
  R.avl = { montar(palco) { palco._pos = null; }, desenhar: (palco, p) => arvore.desenhar(palco, p, true) };

  function montarAbas() {
    const m = lerM();
    q("abas").innerHTML = Object.entries(SIMS).map(([id, s]) => {
      const n = nivelM(m[id]);
      return `<button type="button" role="tab" id="aba-${id}" data-sim="${id}" aria-selected="${id === simId}" style="--tom:${s.tom}">
        <span class="hex">${ic(s.icone)}</span><span class="aba__txt"><b>${esc(s.titulo)}</b><span class="maes" title="${NOMES[n]}">${[1, 2, 3].map((k) => `<i class="${k <= n ? "on" : ""}"></i>`).join("")}</span></span></button>`;
    }).join("");
    q("abas").querySelectorAll("button").forEach((b) => b.addEventListener("click", () => trocarSim(b.dataset.sim)));
    const ativa = q("abas").querySelector("[aria-selected=true]"), faixa = q("abas");
    if (ativa) faixa.scrollLeft = ativa.offsetLeft - (faixa.clientWidth - ativa.offsetWidth) / 2;
  }

  function montarCampos(valores) {
    const s = SIMS[simId];
    q("campos").innerHTML = s.campos.map((c) => `<label class="campo${c.curto ? " campo--curto" : ""}"><span>${esc(c.rot)}</span>
      <input name="${c.id}" type="text" inputmode="numeric" autocomplete="off" value="${esc((valores || {})[c.id] ?? c.padrao)}"></label>`).join("");
  }
  const lerCampos = () => Object.fromEntries([...q("campos").querySelectorAll("input")].map((x) => [x.name, x.value]));

  function trocarSim(id) {
    parar(); simId = id; manual = null;
    try { history.replaceState(null, "", "?" + new URLSearchParams({ ...(consulta.get("embed") ? { embed: "1" } : {}), sim: id })); } catch (e) {  }
    const s = SIMS[id];
    q("simTitulo").textContent = s.titulo; q("simResumo").textContent = s.resumo; q("simFato").textContent = s.fato;
    q("btnManual").hidden = !s.manual;
    document.querySelector(".lab").style.setProperty("--tom", s.tom);
    montarAbas(); montarCampos(); executar();
  }

  function executar(valores) {
    parar(); manual = null; document.querySelector(".lab").classList.remove("lab--manual");
    const s = SIMS[simId], r = s.ler(valores || lerCampos()), erro = q("erroEntrada");
    if (r.erro) { erro.textContent = r.erro; erro.hidden = false; return; }
    erro.hidden = true;
    passos = s.gerar(r.entrada); i = 0; pendente = null; respondidos = new Map(); sessao = { a: 0, t: 0, seq: 0 };
    q("palco").className = "lab__palco lab__palco--" + simId; q("palco")._pos = null;
    q("palco").innerHTML = ""; R[simId].montar(q("palco"), r.entrada);
    q("linha").max = passos.length - 1;
    mostrar(); atualizarPlacar();
  }

  function mostrar() {
    const p = passos[i];
    R[simId].desenhar(q("palco"), p);
    q("narra").textContent = p.txt;
    q("linha").value = i;
    q("passoRotulo").textContent = "Passo " + (i + 1) + " de " + passos.length;
    q("btnInicio").disabled = q("btnAnt").disabled = i === 0 && !pendente;
    q("btnProx").disabled = i >= passos.length - 1 && !pendente;
    q("btnPlay").disabled = !!pendente;
    q("linha").disabled = desafio;
    q("controles").classList.toggle("tocando", tocando);
    q("btnPlay").setAttribute("aria-label", tocando ? "Pausar" : "Reproduzir");
  }

  function fecharPergunta() { pendente = null; q("pergunta").hidden = true; q("pergunta").innerHTML = ""; }

  function irPara(k) { fecharPergunta(); i = Math.max(0, Math.min(passos.length - 1, k)); mostrar(); }

  function avancar() {
    if (pendente) return false;
    if (i >= passos.length - 1) { parar(); return false; }
    const prox = passos[i + 1];
    if (desafio && prox.pergunta && !respondidos.has(i + 1)) { perguntar(i + 1); return false; }
    fecharPergunta(); i++; mostrar(); return true;
  }

  function perguntar(k) {
    pendente = k; parar();
    const pr = passos[k].pergunta, box = q("pergunta");
    box.hidden = false;
    box.innerHTML = `<p class="lab__pergunta-tit">${ic("swords")}<span>Preveja o próximo passo</span></p><p class="lab__pergunta-txt">${esc(pr.enunciado)}</p>
      <div class="lab__opcoes">${pr.opcoes.map((o, n) => `<button class="opcao" type="button" data-n="${n}"><span class="opcao__letra">${String.fromCharCode(65 + n)}</span><span class="opcao__texto">${esc(o)}</span></button>`).join("")}</div>`;
    box.querySelectorAll(".opcao").forEach((b) => b.addEventListener("click", () => responder(k, Number(b.dataset.n))));
    mostrar();
    box.querySelector(".opcao").focus({ preventScroll: true });
  }

  function responder(k, escolha) {
    const pr = passos[k].pergunta, certa = escolha === pr.certa;
    respondidos.set(k, certa);
    sessao.t++; if (certa) { sessao.a++; sessao.seq++; } else sessao.seq = 0;
    const m = lerM(), s = m[simId] || { a: 0, t: 0, seq: 0 };
    s.t++; if (certa) { s.a++; s.seq = (s.seq || 0) + 1; s.melhor = Math.max(s.melhor || 0, s.seq); } else s.seq = 0;
    m[simId] = s; gravarM(m);
    const box = q("pergunta");
    box.querySelectorAll(".opcao").forEach((b, n) => { b.disabled = true; if (n === pr.certa) b.classList.add("certa"); else if (n === escolha) b.classList.add("errada"); });
    box.insertAdjacentHTML("beforeend", `<p class="feedback ${certa ? "ok" : "erro"}">${ic(certa ? "circle-check" : "circle-x")}<span>${certa ? "Acertou! " : "Não foi dessa vez. "}${esc(pr.explica)}</span></p>`);
    pendente = null; i = k; mostrar(); atualizarPlacar(); montarAbas();
  }

  function atualizarPlacar() {
    const m = lerM(), s = m[simId] || { a: 0, t: 0 }, n = nivelM(s);
    q("placar").innerHTML = `<div class="placar__nivel"><span class="maes maes--grande">${[1, 2, 3].map((k) => `<i class="${k <= n ? "on" : ""}"></i>`).join("")}</span>
      <div><b>${NOMES[n]}</b> em ${esc(SIMS[simId].titulo)}<span class="muted">${s.t ? " · " + s.a + " acerto(s) em " + s.t + " previsões" + (s.melhor ? " · melhor sequência " + s.melhor : "") : " · ligue o Modo Desafio e preveja os passos para ganhar maestria"}</span></div></div>
      ${desafio && sessao.t ? `<div class="placar__sessao">Nesta execução: <b>${sessao.a}/${sessao.t}</b>${sessao.seq > 1 ? " · sequência " + sessao.seq : ""}</div>` : ""}
      <p class="muted placar__nota">A maestria fica salva só neste navegador (não é enviada ao servidor).</p>`;
  }

  function tocar() {
    if (tocando) return parar();
    if (i >= passos.length - 1) irPara(0);
    tocando = true; mostrar();
    const atraso = () => [2000, 1300, 800, 380][Number(q("velocidade").value) - 1];
    const tic = () => { if (!tocando) return; if (avancar()) temporizador = setTimeout(tic, atraso()); else parar(); };
    temporizador = setTimeout(tic, 250);
  }
  function parar() { tocando = false; clearTimeout(temporizador); if (passos.length) q("controles").classList.remove("tocando"); }

  function iniciarManual() {
    const r = SIMS.bubble.ler(lerCampos()); if (r.erro) { q("erroEntrada").textContent = r.erro; q("erroEntrada").hidden = false; return; }
    q("erroEntrada").hidden = true; parar(); fecharPergunta();
    const v = r.entrada.valores; let inv = 0;
    for (let a = 0; a < v.length; a++) for (let b = a + 1; b < v.length; b++) if (v[a] > v[b]) inv++;
    manual = { arr: v.map((x, id) => ({ id, v: x })), sel: null, trocas: 0, min: inv, fim: false };
    document.querySelector(".lab").classList.add("lab--manual");
    R.bubble.montar(q("palco"), { valores: v });
    q("palco").querySelectorAll(".barra").forEach((el) => { el.tabIndex = 0; el.setAttribute("role", "button"); el.addEventListener("click", () => cliqueManual(Number(el.dataset.id))); el.addEventListener("keydown", (e) => { if (e.key === "Enter" || e.key === " ") { e.preventDefault(); cliqueManual(Number(el.dataset.id)); } }); });
    pintarManual("Clique em uma barra e depois em uma vizinha para trocá-las. Ordene do menor para o maior com o mínimo de trocas.");
  }
  function cliqueManual(id) {
    if (!manual || manual.fim) return;
    const pos = manual.arr.findIndex((x) => x.id === id);
    if (manual.sel == null) { manual.sel = id; return pintarManual("Agora clique em uma barra vizinha para trocar de lugar (ou na mesma, para desmarcar)."); }
    if (manual.sel === id) { manual.sel = null; return pintarManual("Seleção desfeita."); }
    const ps = manual.arr.findIndex((x) => x.id === manual.sel);
    if (Math.abs(ps - pos) !== 1) { manual.sel = id; return pintarManual("Só vizinhos podem trocar de lugar — como no Bubble Sort. Barra selecionada: " + manual.arr[pos].v + "."); }
    [manual.arr[ps], manual.arr[pos]] = [manual.arr[pos], manual.arr[ps]]; manual.sel = null; manual.trocas++;
    const ordenado = manual.arr.every((x, k) => k === 0 || manual.arr[k - 1].v <= x.v);
    if (ordenado) {
      manual.fim = true;
      const dif = manual.trocas - manual.min;
      return pintarManual(dif === 0 ? "Perfeito! Ordenou com " + manual.trocas + " trocas — o mínimo possível (" + manual.min + ", o número de pares fora de ordem, as \"inversões\"). É exatamente o que o Bubble Sort faz."
        : "Ordenado com " + manual.trocas + " trocas. O mínimo era " + manual.min + " (o número de inversões: pares fora de ordem). Tente de novo com " + dif + " troca(s) a menos.");
    }
    pintarManual("Trocas: " + manual.trocas + " · mínimo possível: " + manual.min + ".");
  }
  function pintarManual(msg) {
    const ordenado = manual.fim;
    manual.arr.forEach((x, pos) => {
      const el = q("palco").querySelector(`.barra[data-id="${x.id}"]`); if (!el) return;
      el.style.setProperty("--i", pos);
      el.className = "barra" + (manual.sel === x.id ? " barra--cmp" : "") + (ordenado ? " barra--ok" : "");
    });
    q("narra").textContent = msg;
  }
  function sairManual() { if (!manual) return; manual = null; document.querySelector(".lab").classList.remove("lab--manual"); executar(); }

  q("entrada").addEventListener("submit", (e) => { e.preventDefault(); executar(); });
  q("btnAleatorio").addEventListener("click", () => { montarCampos(SIMS[simId].aleatorio()); executar(); });
  q("btnManual").addEventListener("click", () => (manual ? sairManual() : iniciarManual()));
  q("btnInicio").addEventListener("click", () => irPara(0));
  q("btnAnt").addEventListener("click", () => { parar(); irPara(i - 1); });
  q("btnProx").addEventListener("click", () => { parar(); avancar(); });
  q("btnPlay").addEventListener("click", tocar);
  q("linha").addEventListener("input", (e) => { parar(); irPara(Number(e.target.value)); });
  q("btnDesafio").addEventListener("click", () => {
    desafio = !desafio; parar(); fecharPergunta();
    q("btnDesafio").setAttribute("aria-pressed", String(desafio));
    sessao = { a: 0, t: 0, seq: 0 }; respondidos = new Map();
    if (desafio) irPara(0); else mostrar();
    atualizarPlacar();
  });
  document.addEventListener("keydown", (e) => {
    if (/INPUT|TEXTAREA/.test((e.target.tagName || "")) && e.target.type !== "range") return;
    if (manual || (e.target.closest && e.target.closest(".opcao"))) return;
    if (e.key === "ArrowRight") { parar(); avancar(); } else if (e.key === "ArrowLeft") { parar(); irPara(i - 1); }
  });

  trocarSim(simId);
})();
