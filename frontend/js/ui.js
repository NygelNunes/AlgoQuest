const Sessao = {
  salvar(u) { sessionStorage.setItem("aq_usuario", JSON.stringify(u)); if (typeof Tema !== "undefined") Tema.aplicar(u); },
  obter() { try { return JSON.parse(sessionStorage.getItem("aq_usuario")); } catch (e) { return null; } },
  limpar() { sessionStorage.removeItem("aq_usuario"); if (typeof Tema !== "undefined") Tema.aplicar(null); },
};
const esc = (v) => { const d = document.createElement("div"); d.textContent = v == null ? "" : String(v); return d.innerHTML; };
const ic = (nome) => `<i class="ic i-${nome}" aria-hidden="true"></i>`;
const AVATAR_PADRAO = "assets/img/avatar-padrao.jpg";
function avatarSrc(u, tam) {
  if (u && u.avatar === "personalizado" && u.avatarImagem) return u.avatarImagem;
  if (u && /^[a-z0-9_]+$/.test(u.avatar || "") && u.avatar !== "personalizado") return "assets/avatares/" + (tam || "g") + "/" + u.avatar + ".webp";
  return AVATAR_PADRAO;
}
const SEM_MOVIMENTO = matchMedia("(prefers-reduced-motion: reduce)").matches;

function contar(el, alvo, sufixo = "") {
  const n = Number(alvo);
  if (SEM_MOVIMENTO || !Number.isFinite(n) || n === 0) { el.textContent = alvo + sufixo; return; }
  const t0 = performance.now(), dur = 900;
  const passo = (t) => {
    const k = Math.min(1, (t - t0) / dur), e = 1 - Math.pow(1 - k, 3);
    el.textContent = Math.round(n * e) + sufixo;
    if (k < 1) requestAnimationFrame(passo);
  };
  requestAnimationFrame(passo);
}
function encher(escopo = document) {
  const barras = escopo.querySelectorAll("[data-largura]");
  requestAnimationFrame(() => requestAnimationFrame(() => barras.forEach((b) => { b.style.width = b.dataset.largura + "%"; })));
}

function montarMenu() {
  const m = document.getElementById("menu");
  if (!m) return;
  const noLab = /laboratorio\.html$/.test(location.pathname), noAdm = /admin\.html$/.test(location.pathname);
  const papel = (Sessao.obter() || {}).papel, ehAdmin = papel === "ADMIN" || papel === "MASTER";
  m.className = "menu superficie";
  m.innerHTML = `<a class="marca" href="modulos.html" aria-label="AlgoQuest">Algo<b>Quest</b></a>
    <nav class="menu__nav" aria-label="Principal">
      <a class="menu__item${noLab || noAdm ? "" : " is-ativo"}" href="modulos.html"${noLab || noAdm ? "" : ' aria-current="page"'} title="Central de Desafios">${ic("swords")}<span>Central de Desafios</span></a>
      <a class="menu__item${noLab ? " is-ativo" : ""}" href="laboratorio.html"${noLab ? ' aria-current="page"' : ""} title="Grimório Vivo">${ic("book-open")}<span>Grimório Vivo</span></a>
      ${ehAdmin ? `<a class="menu__item${noAdm ? " is-ativo" : ""}" href="admin.html"${noAdm ? ' aria-current="page"' : ""} title="Administração">${ic("crown")}<span>Administração</span></a>` : ""}
    </nav>
    <p class="menu__lema">Aprenda. Evolua. Conquiste.</p>
    <button class="menu__sair" id="btnSair" type="button" aria-label="Sair" title="Sair">${ic("log-out")}<span>Sair</span></button>`;
  document.getElementById("btnSair").addEventListener("click", async () => {
    try { await Api.logout(); } catch (e) {  }
    Sessao.limpar();
    window.location.href = "index.html";
  });
}

const Perfil = {
  montar(compacto) {
    const p = document.getElementById("perfil");
    if (!p) return;
    const u = Sessao.obter();
    p.className = "perfil superficie" + (compacto ? " perfil--compacto" : "");
    if (!u) { p.innerHTML = `<p class="muted">Você não está conectado.</p><a class="btn btn--primario" href="index.html">Entrar</a>`; return; }
    const foto = avatarSrc(u, "g"), fotoMini = avatarSrc(u, "p");
    const xp = u.xp ?? 0, nivel = u.nivel ?? 1, meta = u.xpProximoNivel;
    const stats = `<dl class="stats">
        <div>${ic("crown")}<dd>${esc(nivel)}</dd><dt>Nível</dt></div>
        <div>${ic("zap")}<dd>${esc(xp)}</dd><dt>XP</dt></div>
        <div>${ic("star")}<dd>${esc(u.pontuacao ?? 0)}</dd><dt>Pontos</dt></div></dl>`;
    const troca = `<button class="perfil__trocar" id="btnAvatar" type="button" title="Escolher retrato" aria-haspopup="dialog">${ic("camera")}<span class="sr">Escolher retrato do perfil</span></button>`;
    const barraXp = meta ? `<div class="perfil__xp"><div><span>Experiência</span><span>${esc(xp)} / ${esc(meta)} XP</span></div>
        <div class="trilha trilha--xp"><i data-largura="${Math.min(100, (xp / meta) * 100)}"></i></div></div>` : "";
    if (compacto) {
      p.innerHTML = `<details><summary><span class="mini__moldura"><img class="mini" id="retrato" src="${esc(fotoMini)}" alt="Retrato de ${esc(u.nomeUsuario)}"></span>
        <span class="mini__txt"><b>${esc(u.nomeUsuario)}</b><span>Nível ${esc(nivel)} · ${esc(xp)} XP · ${esc(u.pontuacao ?? 0)} pts</span></span>${ic("chevron-down")}</summary>
        <div class="detalhes">${stats}${troca}${barraXp}</div></details>`;
    } else {
      p.innerHTML = `<div class="perfil__moldura"><img class="perfil__retrato" id="retrato" src="${esc(foto)}" alt="Retrato de ${esc(u.nomeUsuario)}">
          <span class="perfil__nivel-selo" title="Nível ${esc(nivel)}"><b>${esc(nivel)}</b></span>${troca}</div>
        <div class="perfil__id"><h2 class="perfil__nome">${esc(u.nomeUsuario)}</h2><p class="perfil__nivel">Nível ${esc(nivel)}</p></div>${barraXp}${stats}
        <div class="perfil__geral" id="progGeral" hidden><div><span>Progresso geral</span><b id="progGeralPct"></b></div><div class="trilha"><i id="progGeralBarra"></i></div></div>`;
    }
    encher(p);
    if (Perfil._pct != null) Perfil.progresso(Perfil._pct);
    document.getElementById("btnAvatar").addEventListener("click", () => {
      if (typeof Avatares === "undefined") return;
      Avatares.abrir();
    });
  },
  atualizarUsuario(novo) {
    const atual = Sessao.obter();
    if (!atual || !novo) return;
    const mudou = ["pontuacao", "xp", "nivel", "xpProximoNivel", "avatar", "avatarImagem", "papel"].some((k) => atual[k] !== novo[k]);
    Sessao.salvar({ ...atual, ...novo });
    if (mudou) { montarPerfilAtual(); if (atual.papel !== novo.papel) montarMenu(); }
  },
  progresso(pct) {
    Perfil._pct = pct;
    const g = document.getElementById("progGeral");
    if (!g) return;
    g.hidden = false;
    document.getElementById("progGeralPct").textContent = pct + "%";
    const b = document.getElementById("progGeralBarra");
    b.dataset.largura = pct; encher(g);
  },
};
montarMenu();
const perfilCompacto = document.body.dataset.perfil === "compacto";
const telaLarga = matchMedia("(min-width:1280px)");
const montarPerfilAtual = () => Perfil.montar(perfilCompacto && !telaLarga.matches);
montarPerfilAtual();
if (perfilCompacto) telaLarga.addEventListener("change", montarPerfilAtual);
Api.eu().then((u) => Perfil.atualizarUsuario(u)).catch(() => {});
