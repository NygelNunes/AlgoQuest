(() => {
  const $ = (id) => document.getElementById(id);
  const ROTULO_PAPEL = { MASTER: "Master", ADMIN: "Administrador", USUARIO: "Usuário comum" };
  const DIF = { FACIL: "Fácil", MEDIO: "Médio", DIFICIL: "Difícil" };
  const TEMA_NOME = { arvore: "Árvores", busca: "Busca", ordenacao: "Ordenação", hash: "Hash", analise: "Análise", padrao: "Clássico" };
  let papel = null, aba = "modulos", modulos = [], moduloSel = null;

  let tAviso;
  function aviso(texto, erro) {
    const a = $("admAviso"); a.textContent = texto; a.hidden = false; a.className = "adm__aviso" + (erro ? " adm__aviso--erro" : "");
    clearTimeout(tAviso); tAviso = setTimeout(() => (a.hidden = true), erro ? 7000 : 3500);
  }
  const erroMsg = (e) => e.mensagem || (e.status === 403 ? "Você não tem permissão para esta ação." : e.status === 401 ? "Sessão expirada." : "Não foi possível concluir. Tente novamente.");
  const arte = (tema) => `<div class="missao__arte adm__arte" style="--tom:${TEMAS[tema].tom}">${arteSvg(tema)}</div>`;

  function dialogo(html, classe) {
    const d = document.createElement("dialog"); d.className = "adm__dlg " + (classe || "");
    d.innerHTML = `<div class="adm__caixa">${html}</div>`; document.body.appendChild(d);
    d.addEventListener("close", () => d.remove());
    d.addEventListener("click", (e) => { if (e.target === d && !d.dataset.ocupado) d.close(); });
    d.showModal(); return d;
  }
  function confirmar(titulo, texto, rotulo, perigo = true) {
    return new Promise((res) => {
      const d = dialogo(`<h2>${esc(titulo)}</h2><p class="adm__txt">${esc(texto)}</p>
        <div class="adm__rodape"><button class="btn btn--fantasma" data-n type="button">Cancelar</button><button class="btn ${perigo ? "adm__perigo" : "btn--primario"}" data-s type="button">${esc(rotulo)}</button></div>`, "adm__dlg--peq");
      let r = false; d.querySelector("[data-n]").onclick = () => d.close();
      d.querySelector("[data-s]").onclick = () => { r = true; d.close(); };
      d.addEventListener("close", () => res(r)); d.querySelector("[data-n]").focus();
    });
  }
  async function acao(fn, ok) { try { await fn(); if (ok) aviso(ok); return true; } catch (e) { aviso(erroMsg(e), true); return false; } }

  function abas() {
    const lista = [["modulos", "Módulos"], ["questoes", "Questões"], ["importar", "Importar"]];
    if (papel === "MASTER") lista.push(["usuarios", "Usuários"]);
    $("admAbas").innerHTML = lista.map(([k, n]) => `<button type="button" role="tab" class="adm__aba" data-aba="${k}" aria-selected="${k === aba}" tabindex="${k === aba ? 0 : -1}">${n}</button>`).join("");
    $("admAbas").querySelectorAll("button").forEach((b) => (b.onclick = () => { aba = b.dataset.aba; abas(); desenhar(); }));
  }
  async function desenhar() {
    const p = $("admPainel"); p.innerHTML = '<p class="estado">Carregando...</p>';
    try {
      if (aba !== "usuarios") modulos = (await Api.admModulos()) || [];
      if (aba === "modulos") abaModulos(p); else if (aba === "questoes") await abaQuestoes(p); else if (aba === "importar") abaImportar(p); else await abaUsuarios(p);
    } catch (e) { p.innerHTML = `<p class="estado">${esc(erroMsg(e))}</p>`; }
  }

  function abaModulos(p) {
    p.innerHTML = `<div class="adm__barra"><h2>Módulos</h2><button class="btn btn--primario" id="novoMod" type="button">Novo módulo</button></div>
      <p class="adm__dica">A ordem aqui é a ordem da Central de Desafios. Módulos inativos ficam ocultos para os jogadores.</p>
      <ul class="adm__lista">${modulos.map((m, i) => `<li class="adm__item${m.ativo ? "" : " is-inativo"}">
        ${arte(temaDoModulo(m))}
        <div class="adm__info"><h3>${esc(m.titulo)}${m.ativo ? "" : ' <span class="adm__tag">Inativo</span>'}</h3><p>${esc(m.descricao)}</p><small>${m.quantidade} questões · ilustração: ${esc(TEMA_NOME[temaDoModulo(m)])}</small></div>
        <div class="adm__acoes">
          <button class="adm__ic" data-mv="${m.id}" data-d="-1" aria-label="Mover para cima" ${i === 0 ? "disabled" : ""}>↑</button>
          <button class="adm__ic" data-mv="${m.id}" data-d="1" aria-label="Mover para baixo" ${i === modulos.length - 1 ? "disabled" : ""}>↓</button>
          <button class="btn btn--fantasma" data-q="${m.id}" type="button">Questões</button>
          <button class="btn btn--fantasma" data-ed="${m.id}" type="button">Editar</button>
          <button class="btn adm__perigo" data-ex="${m.id}" type="button">Excluir</button></div></li>`).join("") || '<li class="estado">Nenhum módulo ainda.</li>'}</ul>`;
    $("novoMod").onclick = () => formModulo(null);
    p.querySelectorAll("[data-ed]").forEach((b) => (b.onclick = () => formModulo(modulos.find((m) => m.id == b.dataset.ed))));
    p.querySelectorAll("[data-q]").forEach((b) => (b.onclick = () => { moduloSel = +b.dataset.q; aba = "questoes"; abas(); desenhar(); }));
    p.querySelectorAll("[data-mv]").forEach((b) => (b.onclick = async () => { await acao(() => Api.admMoverModulo(b.dataset.mv, +b.dataset.d)); desenhar(); }));
    p.querySelectorAll("[data-ex]").forEach((b) => (b.onclick = async () => {
      const m = modulos.find((x) => x.id == b.dataset.ex);
      if (!(await confirmar("Excluir módulo", `Excluir "${m.titulo}" apaga também suas ${m.quantidade} questões e o progresso dos jogadores nelas. Esta ação não pode ser desfeita.`, "Excluir módulo"))) return;
      if (await acao(() => Api.admExcluirModulo(m.id), "Módulo excluído.") && moduloSel === m.id) moduloSel = null; desenhar();
    }));
  }

  function formModulo(m) {
    let tema = m ? temaDoModulo(m) : "padrao";
    const d = dialogo(`<h2>${m ? "Editar módulo" : "Novo módulo"}</h2>
      <form id="fm" class="adm__form" novalidate>
        <label>Nome<input id="fmT" maxlength="120" value="${esc(m ? m.titulo : "")}" required></label>
        <label>Descrição<textarea id="fmD" rows="3" maxlength="500" required>${esc(m ? m.descricao : "")}</textarea></label>
        <fieldset class="adm__temas"><legend>Ilustração de fundo</legend><div id="fmTemas">${Object.keys(TEMAS).map((k) =>
          `<label class="adm__tema"><input type="radio" name="tema" value="${k}"${k === tema ? " checked" : ""}>${arte(k)}<span>${TEMA_NOME[k]}</span></label>`).join("")}</div></fieldset>
        <label class="adm__check"><input type="checkbox" id="fmA"${!m || m.ativo ? " checked" : ""}> Módulo ativo (visível aos jogadores)</label>
        <p class="adm__erro" id="fmE" role="alert" hidden></p>
        <div class="adm__rodape"><button class="btn btn--fantasma" type="button" data-n>Cancelar</button><button class="btn btn--primario" type="submit">Salvar</button></div></form>`);
    d.querySelector("[data-n]").onclick = () => d.close(); d.querySelector("#fmT").focus();
    d.querySelector("#fm").onsubmit = async (ev) => {
      ev.preventDefault(); const e = d.querySelector("#fmE"); e.hidden = true;
      const corpo = { titulo: d.querySelector("#fmT").value.trim(), descricao: d.querySelector("#fmD").value.trim(), tema: d.querySelector("input[name=tema]:checked").value, ativo: d.querySelector("#fmA").checked };
      if (corpo.titulo.length < 2) { e.textContent = "Informe o nome do módulo."; e.hidden = false; return; }
      if (!corpo.descricao) { e.textContent = "Informe a descrição."; e.hidden = false; return; }
      try { await (m ? Api.admEditarModulo(m.id, corpo) : Api.admCriarModulo(corpo)); d.close(); aviso(m ? "Módulo atualizado." : "Módulo criado."); desenhar(); }
      catch (x) { e.textContent = erroMsg(x); e.hidden = false; }
    };
  }

  const seletorModulo = (id) => `<label class="adm__sel">Módulo<select id="${id}">${modulos.map((m) => `<option value="${m.id}"${m.id === moduloSel ? " selected" : ""}>${esc(m.titulo)}</option>`).join("")}</select></label>`;
  async function abaQuestoes(p) {
    if (!modulos.length) { p.innerHTML = '<p class="estado">Crie um módulo primeiro.</p>'; return; }
    if (!modulos.some((m) => m.id === moduloSel)) moduloSel = modulos[0].id;
    const qs = (await Api.admQuestoes(moduloSel)) || [];
    p.innerHTML = `<div class="adm__barra"><h2>Questões</h2><button class="btn btn--primario" id="novaQ" type="button">Nova questão</button></div>
      ${seletorModulo("selMod")}
      <ol class="adm__lista adm__lista--q">${qs.map((q, i) => `<li class="adm__item adm__item--q">
        <span class="adm__num">${i + 1}</span>
        <div class="adm__info"><h3>${esc(q.descricao)}</h3><small><span class="adm__pill adm__pill--${q.dificuldade}">${DIF[q.dificuldade]}</span> ${q.pontos} pts · ${q.opcoes.length} alternativas · correta: ${String.fromCharCode(65 + q.indiceCorreto)}${q.visual ? " · com visual" : ""}</small></div>
        <div class="adm__acoes"><button class="adm__ic" data-mv="${q.id}" data-d="-1" aria-label="Mover para cima" ${i === 0 ? "disabled" : ""}>↑</button>
          <button class="adm__ic" data-mv="${q.id}" data-d="1" aria-label="Mover para baixo" ${i === qs.length - 1 ? "disabled" : ""}>↓</button>
          <button class="btn btn--fantasma" data-ed="${q.id}" type="button">Editar</button><button class="btn adm__perigo" data-ex="${q.id}" type="button">Excluir</button></div></li>`).join("") || '<li class="estado">Este módulo ainda não tem questões.</li>'}</ol>`;
    $("selMod").onchange = (e) => { moduloSel = +e.target.value; desenhar(); };
    $("novaQ").onclick = () => formQuestao(null);
    p.querySelectorAll("[data-ed]").forEach((b) => (b.onclick = () => formQuestao(qs.find((q) => q.id == b.dataset.ed))));
    p.querySelectorAll("[data-mv]").forEach((b) => (b.onclick = async () => { await acao(() => Api.admMoverQuestao(b.dataset.mv, +b.dataset.d)); desenhar(); }));
    p.querySelectorAll("[data-ex]").forEach((b) => (b.onclick = async () => {
      if (!(await confirmar("Excluir questão", "A questão e o progresso dos jogadores nela serão apagados. Esta ação não pode ser desfeita.", "Excluir questão"))) return;
      await acao(() => Api.admExcluirQuestao(b.dataset.ex), "Questão excluída."); desenhar();
    }));
  }

  function lerImagem(arq) {
    return new Promise((res, rej) => {
      if (!["image/jpeg", "image/png", "image/webp"].includes(arq.type)) return rej(new Error("Use uma imagem JPEG, PNG ou WebP."));
      if (arq.size > 8 * 1024 * 1024) return rej(new Error("Imagem grande demais (máximo 8 MB)."));
      const url = URL.createObjectURL(arq), img = new Image();
      img.onerror = () => { URL.revokeObjectURL(url); rej(new Error("Não foi possível ler esta imagem.")); };
      img.onload = () => {
        URL.revokeObjectURL(url);
        const k = Math.min(1, 900 / Math.max(img.naturalWidth, img.naturalHeight)), c = document.createElement("canvas");
        c.width = Math.round(img.naturalWidth * k); c.height = Math.round(img.naturalHeight * k);
        const x = c.getContext("2d"); x.fillStyle = "#fff"; x.fillRect(0, 0, c.width, c.height); x.drawImage(img, 0, 0, c.width, c.height);
        let q = .88, d; do { d = c.toDataURL("image/jpeg", q); q -= .12; } while (d.length * 3 / 4 > 280 * 1024 && q > .3);
        res(d);
      };
      img.src = url;
    });
  }

  function formQuestao(q) {
    let ops = q ? [...q.opcoes] : ["", ""], correta = q ? q.indiceCorreto : 0;
    let modoVis = !q || !q.visual ? "nenhum" : q.visual.startsWith("data:") ? "imagem" : "svg", imagem = modoVis === "imagem" ? q.visual : null;
    const d = dialogo(`<h2>${q ? "Editar questão" : "Nova questão"}</h2>
      <form id="fq" class="adm__form" novalidate>
        <label>Enunciado<textarea id="fqD" rows="3" maxlength="1000">${esc(q ? q.descricao : "")}</textarea></label>
        <div class="adm__duo"><label>Dificuldade<select id="fqF">${Object.entries(DIF).map(([k, n]) => `<option value="${k}"${q && q.dificuldade === k ? " selected" : ""}>${n}</option>`).join("")}</select></label>
          <label>Pontos<input id="fqP" type="number" min="1" max="1000" value="${q ? q.pontos : 10}"></label></div>
        <fieldset class="adm__alts"><legend>Alternativas (marque a correta)</legend><div id="fqO"></div><button class="btn btn--fantasma" id="fqAdd" type="button">Adicionar alternativa</button></fieldset>
        <label>Explicação (mostrada ao acertar)<textarea id="fqE" rows="2" maxlength="1000">${esc(q && q.explicacao ? q.explicacao : "")}</textarea></label>
        <fieldset class="adm__vis"><legend>Recurso visual (opcional)</legend>
          <div class="adm__seg" role="radiogroup">${[["nenhum", "Nenhum"], ["imagem", "Imagem"], ["svg", "SVG"]].map(([k, n]) => `<label><input type="radio" name="vis" value="${k}"${k === modoVis ? " checked" : ""}><span>${n}</span></label>`).join("")}</div>
          <div id="fqV"></div></fieldset>
        <p class="adm__erro" id="fqErro" role="alert" hidden></p>
        <div class="adm__rodape"><button class="btn btn--fantasma" type="button" data-n>Cancelar</button><button class="btn btn--primario" type="submit">Salvar</button></div></form>`, "adm__dlg--larga");
    const $d = (s) => d.querySelector(s), erro = (t) => { const e = $d("#fqErro"); e.textContent = t || ""; e.hidden = !t; };
    d.querySelector("[data-n]").onclick = () => d.close();
    function opcoes() {
      $d("#fqO").innerHTML = ops.map((o, i) => `<div class="adm__alt"><input type="radio" name="cor" value="${i}" aria-label="Alternativa ${String.fromCharCode(65 + i)} é a correta"${i === correta ? " checked" : ""}>
        <span class="adm__letra">${String.fromCharCode(65 + i)}</span><input type="text" data-o="${i}" maxlength="500" value="${esc(o)}" aria-label="Texto da alternativa ${String.fromCharCode(65 + i)}">
        <button class="adm__ic" type="button" data-rm="${i}" aria-label="Remover alternativa" ${ops.length <= 2 ? "disabled" : ""}>×</button></div>`).join("");
      $d("#fqAdd").disabled = ops.length >= 6;
      d.querySelectorAll("[data-o]").forEach((i) => (i.oninput = () => (ops[+i.dataset.o] = i.value)));
      d.querySelectorAll("input[name=cor]").forEach((r) => (r.onchange = () => (correta = +r.value)));
      d.querySelectorAll("[data-rm]").forEach((b) => (b.onclick = () => { const i = +b.dataset.rm; ops.splice(i, 1); if (correta === i) correta = 0; else if (correta > i) correta--; opcoes(); }));
    }
    $d("#fqAdd").onclick = () => { ops.push(""); opcoes(); d.querySelector(`[data-o="${ops.length - 1}"]`).focus(); };
    opcoes();
    function visual() {
      const box = $d("#fqV");
      if (modoVis === "nenhum") box.innerHTML = '<p class="adm__dica">Sem imagem ou diagrama neste enunciado.</p>';
      else if (modoVis === "imagem") {
        box.innerHTML = `<input type="file" id="fqArq" accept="image/jpeg,image/png,image/webp"><div class="adm__prev" id="fqPrev">${imagem ? `<img src="${esc(imagem)}" alt="Pré-visualização">` : ""}</div>`;
        $d("#fqArq").onchange = async (e) => { try { erro(""); imagem = await lerImagem(e.target.files[0]); $d("#fqPrev").innerHTML = `<img src="${esc(imagem)}" alt="Pré-visualização">`; } catch (x) { erro(x.message); } };
      } else {
        box.innerHTML = `<textarea id="fqSvg" rows="6" spellcheck="false" placeholder='<svg viewBox="0 0 200 100"> ... </svg>'>${esc(q && q.visual && !q.visual.startsWith("data:") ? q.visual : "")}</textarea>
          <p class="adm__dica">Somente formas e texto (sem script, links externos ou imagens embutidas). Prefira traços claros: o fundo é escuro.</p><div class="adm__prev" id="fqPrev"></div>`;
        const pv = () => { const s = visualSrc($d("#fqSvg").value); $d("#fqPrev").innerHTML = s ? `<img src="${esc(s)}" alt="Pré-visualização do SVG">` : ""; };
        $d("#fqSvg").oninput = pv; pv();
      }
    }
    d.querySelectorAll("input[name=vis]").forEach((r) => (r.onchange = () => { modoVis = r.value; erro(""); visual(); }));
    visual(); $d("#fqD").focus();
    $d("#fq").onsubmit = async (ev) => {
      ev.preventDefault(); erro("");
      const corpo = { descricao: $d("#fqD").value, dificuldade: $d("#fqF").value, pontos: parseInt($d("#fqP").value, 10) || null, opcoes: ops, indiceCorreto: correta, explicacao: $d("#fqE").value,
        visual: modoVis === "imagem" ? imagem : modoVis === "svg" ? $d("#fqSvg").value : null };
      if (corpo.descricao.trim().length < 5) return erro("Escreva o enunciado (mínimo 5 caracteres).");
      if (ops.some((o) => !o.trim())) return erro("Preencha todas as alternativas ou remova as vazias.");
      if (modoVis === "imagem" && !imagem) return erro("Escolha uma imagem ou selecione “Nenhum”.");
      try { await (q ? Api.admEditarQuestao(q.id, corpo) : Api.admCriarQuestao(moduloSel, corpo)); d.close(); aviso(q ? "Questão atualizada." : "Questão criada."); desenhar(); }
      catch (x) { erro(erroMsg(x)); }
    };
  }

  const MODELO = { modulo: "Nome do módulo de destino (opcional, confere com o destino escolhido)", questoes: [
    { descricao: "Qual é a complexidade da busca binária no pior caso?", dificuldade: "MEDIO", pontos: 20, opcoes: ["O(n)", "O(1)", "O(log n)", "O(n²)"], indiceCorreto: 2, explicacao: "A cada passo, metade do espaço é descartada." },
    { descricao: "Pergunta com diagrama SVG (visual é opcional)", dificuldade: "FACIL", opcoes: ["Sim", "Não"], indiceCorreto: 0, visual: "<svg viewBox=\"0 0 120 60\"><circle cx=\"30\" cy=\"30\" r=\"14\" fill=\"none\" stroke=\"#dcc084\" stroke-width=\"3\"/><path d=\"M44 30H90\" stroke=\"#dcc084\" stroke-width=\"3\"/></svg>" }] };
  function normalizar(bruto) {
    const lista = Array.isArray(bruto) ? bruto : bruto && bruto.questoes;
    if (!Array.isArray(lista)) throw new Error("Formato inválido: esperado um objeto com a lista \"questoes\" (ou uma lista de questões).");
    return { modulo: Array.isArray(bruto) ? null : bruto.modulo || null, questoes: lista.map((q) => (q && typeof q === "object" ? {
      descricao: q.descricao ?? q.enunciado ?? q.pergunta, dificuldade: q.dificuldade, pontos: q.pontos ?? null,
      opcoes: q.opcoes ?? q.alternativas, indiceCorreto: q.indiceCorreto ?? q.correta ?? null, explicacao: q.explicacao ?? q.feedback ?? null, visual: q.visual ?? null } : q)) };
  }
  function abaImportar(p) {
    if (!modulos.length) { p.innerHTML = '<p class="estado">Crie um módulo primeiro.</p>'; return; }
    if (!modulos.some((m) => m.id === moduloSel)) moduloSel = modulos[0].id;
    let dados = null;
    p.innerHTML = `<div class="adm__barra"><h2>Importar banco de questões</h2><button class="btn btn--fantasma" id="modelo" type="button">Baixar modelo JSON</button></div>
      <p class="adm__dica">Cada arquivo é importado para <b>um</b> módulo. Tudo é validado antes de gravar; se houver qualquer erro, nada é importado.</p>
      <div class="adm__duo">${seletorModulo("impMod")}<label class="adm__sel">Arquivo (.json, até 1 MB)<input type="file" id="impArq" accept=".json,application/json"></label></div>
      <label class="adm__check"><input type="checkbox" id="impDup" checked> Ignorar questões duplicadas (mesmo enunciado)</label>
      <div id="impRes" class="adm__res" aria-live="polite"></div>`;
    $("modelo").onclick = () => { const a = document.createElement("a"); a.href = URL.createObjectURL(new Blob([JSON.stringify(MODELO, null, 2)], { type: "application/json" })); a.download = "modelo-importacao.json"; a.click(); setTimeout(() => URL.revokeObjectURL(a.href), 1000); };
    $("impMod").onchange = (e) => { moduloSel = +e.target.value; if (dados) validar(); };
    $("impDup").onchange = () => dados && validar();
    $("impArq").onchange = async (e) => {
      const arq = e.target.files[0]; dados = null; $("impRes").innerHTML = "";
      if (!arq) return;
      if (arq.size > 1024 * 1024) return resultadoErro("Arquivo grande demais (máximo 1 MB).");
      try { dados = normalizar(JSON.parse(await arq.text())); } catch (x) { return resultadoErro(x instanceof SyntaxError ? "O arquivo não é um JSON válido." : x.message); }
      validar();
    };
    const resultadoErro = (t) => { $("impRes").innerHTML = `<p class="adm__erro" role="alert">${esc(t)}</p>`; };
    async function validar() {
      $("impRes").innerHTML = '<p class="estado">Validando...</p>';
      try {
        const r = await Api.admImportar(moduloSel, { ...dados, confirmar: false, ignorarDuplicadas: $("impDup").checked });
        const ok = !r.erros.length && r.validas > 0;
        $("impRes").innerHTML = `<div class="adm__resumo"><span><b>${r.total}</b> no arquivo</span><span class="adm__ok"><b>${r.validas}</b> válidas</span><span><b>${r.duplicadas}</b> duplicadas</span><span class="${r.erros.length ? "adm__bad" : ""}"><b>${r.erros.length}</b> erros</span></div>
          ${r.erros.length ? `<ul class="adm__erros">${r.erros.slice(0, 50).map((e) => `<li>${e.indice ? "Questão " + e.indice + ": " : ""}${esc(e.mensagem)}</li>`).join("")}</ul>` : ""}
          ${r.previa.length ? `<h3>Pré-visualização${r.previa.length > 10 ? " (10 primeiras)" : ""}</h3><ol class="adm__previa">${r.previa.slice(0, 10).map((q) => `<li><b>${esc(q.descricao)}</b><small>${DIF[q.dificuldade]} · ${q.pontos} pts · correta: ${esc(q.opcoes[q.indiceCorreto])}${q.visual ? " · com visual" : ""}</small></li>`).join("")}</ol>` : ""}
          <div class="adm__rodape"><button class="btn btn--primario" id="impOk" type="button" ${ok ? "" : "disabled"}>Importar ${r.validas} questões</button></div>`;
        $("impOk").onclick = async () => {
          const alvo = modulos.find((m) => m.id === moduloSel);
          if (!(await confirmar("Confirmar importação", `Importar ${r.validas} questões para o módulo "${alvo.titulo}"?`, "Importar", false))) return;
          try {
            const f = await Api.admImportar(moduloSel, { ...dados, confirmar: true, ignorarDuplicadas: $("impDup").checked });
            if (f.importado) { aviso(`${f.importadas} questões importadas para "${alvo.titulo}".`); dados = null; $("impArq").value = ""; $("impRes").innerHTML = `<p class="estado">${f.importadas} questões importadas. Elas já estão disponíveis para os jogadores.</p>`; }
            else aviso("Nada foi importado: corrija os erros do arquivo.", true);
          } catch (x) { aviso(erroMsg(x), true); }
        };
      } catch (x) { resultadoErro(erroMsg(x)); }
    }
  }

  async function abaUsuarios(p) {
    if (papel !== "MASTER") { p.innerHTML = '<p class="estado">Acesso exclusivo do Master.</p>'; return; }
    const us = (await Api.admUsuarios()) || [], eu = Sessao.obter() || {};
    p.innerHTML = `<div class="adm__barra"><h2>Usuários</h2></div><p class="adm__dica">Somente o Master altera papéis. A conta Master é protegida.</p>
      <div class="adm__tabela" role="table" aria-label="Usuários"><div class="adm__lin adm__lin--cab" role="row"><span role="columnheader">Usuário</span><span role="columnheader">E-mail</span><span role="columnheader">Papel</span><span role="columnheader">Ação</span></div>
      ${us.map((u) => `<div class="adm__lin" role="row"><span role="cell"><b>${esc(u.nomeUsuario)}</b><small>Nível ${u.nivel} · ${u.pontuacao} pts</small></span><span role="cell" class="adm__mudo">${u.email ? esc(u.email) : "—"}</span>
        <span role="cell"><span class="adm__papelTag adm__papelTag--${u.papel}">${ROTULO_PAPEL[u.papel] || esc(u.papel)}</span></span>
        <span role="cell">${u.papel === "MASTER" ? '<small class="adm__mudo">Protegido</small>' : u.nomeUsuario === eu.nomeUsuario ? '<small class="adm__mudo">Você</small>'
          : u.papel === "ADMIN" ? `<button class="btn btn--fantasma" data-p="${u.id}" data-novo="USUARIO" data-n="${esc(u.nomeUsuario)}" type="button">Tornar usuário comum</button>`
          : `<button class="btn btn--primario" data-p="${u.id}" data-novo="ADMIN" data-n="${esc(u.nomeUsuario)}" type="button">Promover a administrador</button>`}</span></div>`).join("")}</div>`;
    p.querySelectorAll("[data-p]").forEach((b) => (b.onclick = async () => {
      const promover = b.dataset.novo === "ADMIN";
      if (!(await confirmar(promover ? "Promover a administrador" : "Remover privilégios", promover ? `${b.dataset.n} poderá gerenciar módulos e questões.` : `${b.dataset.n} voltará a ser usuário comum e perderá o acesso à administração.`, promover ? "Promover" : "Confirmar", !promover))) return;
      await acao(() => Api.admPapel(b.dataset.p, b.dataset.novo), "Papel atualizado."); desenhar();
    }));
  }

  (async () => {
    try { const u = await Api.eu(); Perfil.atualizarUsuario(u); papel = u.papel; } catch (e) { return; }
    if (papel !== "ADMIN" && papel !== "MASTER") { location.replace("modulos.html"); return; }
    $("admPapel").textContent = ROTULO_PAPEL[papel];
    abas(); desenhar();
  })();
})();
