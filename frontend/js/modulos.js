const jogador = Sessao.obter();
if (jogador) document.getElementById("boasVindas").textContent = "Bem-vindo, " + jogador.nomeUsuario + "!";

const dadosModulo = (m) => {
  const tema = temaDoModulo(m);
  const pct = Math.max(0, Math.min(100, Math.round(m.porcentagem || 0)));
  return { tema, tom: TEMAS[tema].tom, icone: TEMAS[tema].icone,
    qtd: m.quantidadeExercicios ?? (m.exercicios ? m.exercicios.length : null),
    pct, feito: pct >= 100, href: "exercicio.html?modulo=" + encodeURIComponent(m.id) };
};
const hex = (icone) => `<span class="hex">${ic(icone)}</span>`;
const rotuloAcao = (d) => (d.feito ? "Revisitar módulo" : d.pct > 0 ? "Continuar aventura" : "Iniciar aventura");

function cartao(m, destaque) {
  const d = dadosModulo(m);
  return `<article class="missao${destaque ? " missao--destaque" : ""}${d.feito ? " missao--feita" : ""}" style="--tom:${d.tom}">
    <div class="missao__arte">${arteSvg(d.tema)}<span class="missao__simbolo">${hex(d.icone)}</span>
      ${d.feito ? `<span class="selo">${ic("trophy")}Concluído</span>` : ""}</div>
    <div class="missao__corpo">
      ${destaque ? '<span class="etiqueta">Próxima parada da sua jornada</span>' : ""}
      <h3>${esc(m.titulo)}</h3><p class="missao__desc">${esc(m.descricao)}</p>
      <div class="missao__meta"><span>${d.qtd != null ? esc(d.qtd) + " desafios" : ""}</span><span>${d.pct}%</span></div>
      <div class="trilha" role="progressbar" aria-label="Progresso em ${esc(m.titulo)}" aria-valuenow="${d.pct}" aria-valuemin="0" aria-valuemax="100"><i data-largura="${d.pct}"></i></div>
      <a class="btn ${destaque ? "btn--primario" : "btn--fantasma"} btn--bloco" href="${d.href}">${rotuloAcao(d)} ${ic("arrow-right")}</a>
    </div></article>`;
}

function mapa(mods, alvo) {
  const P = 150, H = mods.length * P;
  const pos = mods.map((_, i) => ({ x: i % 2 ? 70 : 30, y: i * P + P / 2 }));
  const trecho = (i) => `M${pos[i - 1].x} ${pos[i - 1].y}C${pos[i - 1].x} ${pos[i].y - P / 2 + 14},${pos[i].x} ${pos[i - 1].y + P / 2 - 14},${pos[i].x} ${pos[i].y}`;
  let trilhas = "";
  for (let i = 1; i < mods.length; i++) {
    const conquistado = dadosModulo(mods[i - 1]).feito;
    trilhas += `<path class="trecho" d="${trecho(i)}"/>` + (conquistado ? `<path class="trecho trecho--ouro" d="${trecho(i)}"/>` : "");
  }
  const C = 2 * Math.PI * 26;
  const nos = mods.map((m, i) => {
    const d = dadosModulo(m);
    return `<a class="no${i === alvo ? " no--alvo" : ""}${d.feito ? " no--feito" : ""}" href="${d.href}" style="left:${pos[i].x}%;top:${pos[i].y + 16}px;--tom:${d.tom}" aria-label="${esc(m.titulo)}, ${d.pct}%">
      <span class="no__selo"><svg class="no__anel" viewBox="0 0 60 60" aria-hidden="true"><circle class="a0" cx="30" cy="30" r="26"/><circle class="a1" cx="30" cy="30" r="26" pathLength="100" stroke-dasharray="${d.pct} 100"/></svg>${hex(d.icone)}${d.feito ? `<span class="no__ok">${ic("circle-check")}</span>` : ""}</span>
      <span class="no__placa"><strong>${esc(m.titulo)}</strong>
      <span class="no__meta">${d.pct}%${d.qtd != null ? " · " + esc(d.qtd) + " desafios" : ""}</span></span>
      ${i === alvo ? `<span class="no__aqui">${ic("flag")}Você está aqui</span>` : ""}</a>`;
  }).join("");
  return `<div class="mapa superficie" style="height:${H + 32}px"><svg class="mapa__trilha" viewBox="0 0 100 ${H}" preserveAspectRatio="none" aria-hidden="true">${trilhas}</svg>${nos}</div>`;
}

function visaoGeral(mods) {
  const todos = mods.every((m) => (m.quantidadeExercicios ?? (m.exercicios ? m.exercicios.length : null)) != null);
  const total = todos ? mods.reduce((s, m) => s + (m.quantidadeExercicios ?? m.exercicios.length), 0) : null;
  const media = Math.round(mods.reduce((s, m) => s + (m.porcentagem || 0), 0) / mods.length);
  const itens = [[mods.length, "Módulos", ""], [total, "Desafios", ""], [mods.filter((m) => m.porcentagem >= 100).length, "Módulos concluídos", ""], [media, "Progresso geral", "%"]];
  const caixa = document.getElementById("visaoGeral");
  caixa.innerHTML = itens.filter(([v]) => v != null).map(([v, r, s]) =>
    `<div><dd data-n="${esc(v)}" data-s="${s}">${esc(v)}${s}</dd><dt>${r}</dt>${s ? `<span class="visao__fio"><i data-largura="${v}"></i></span>` : ""}</div>`).join("");
  caixa.querySelectorAll("dd").forEach((dd) => contar(dd, dd.dataset.n, dd.dataset.s));
  encher(caixa);
  Perfil.progresso(media);
}

async function carregarModulos() {
  const lista = document.getElementById("listaModulos");
  try {
    const mods = (await Api.listarModulos()) || [];
    if (mods.length === 0) { lista.innerHTML = '<p class="estado">Nenhum módulo disponível ainda.</p>'; return; }
    let alvo = mods.findIndex((m) => m.porcentagem > 0 && m.porcentagem < 100);
    if (alvo < 0) alvo = mods.findIndex((m) => !(m.porcentagem >= 100));
    if (alvo < 0) alvo = 0;
    visaoGeral(mods);
    const da = dadosModulo(mods[alvo]), botaoContinuar = document.getElementById("btnContinuar");
    botaoContinuar.href = da.href;
    botaoContinuar.innerHTML = `<span>${da.feito ? "Revisitar" : da.pct > 0 ? "Continuar" : "Começar"}: ${esc(mods[alvo].titulo)}</span>${ic("arrow-right")}`;
    botaoContinuar.hidden = false;
    const html = {
      cartoes: '<div class="grade">' + cartao(mods[alvo], true) + mods.map((m, i) => (i === alvo ? "" : cartao(m, false))).join("") + "</div>",
      mapa: mapa(mods, alvo),
    };
    let atualVista = null;
    const mostrar = (v) => {
      if (v === atualVista) return;
      atualVista = v;
      lista.innerHTML = `<div class="vista vista--${v}">${html[v]}</div>`;
      encher(lista);
      document.querySelectorAll("#alterna button").forEach((b) => b.setAttribute("aria-pressed", String(b.dataset.vista === v)));
    };
    document.querySelectorAll("#alterna button").forEach((b) => b.addEventListener("click", () => mostrar(b.dataset.vista)));
    mostrar("cartoes");
  } catch (e) {
    lista.innerHTML = '<p class="estado">Erro ao carregar módulos.</p>';
  }
}
carregarModulos();
