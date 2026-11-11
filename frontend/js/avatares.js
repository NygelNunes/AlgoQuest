const Avatares = (() => {
  const MAX_ARQ = 5 * 1024 * 1024, TIPOS = ["image/jpeg", "image/png", "image/webp"], VISOR = 280, SAIDA = 384, MAX_SAIDA = 140 * 1024;
  let dlg, estado;

  const nomeDe = (id) => (AVATAR_CATALOGO.find((a) => a.id === id) || {}).nome || "";
  const catDe = (id) => (AVATAR_CATALOGO.find((a) => a.id === id) || {}).cat;

  function abrir() {
    const u = Sessao.obter() || {};
    const atual = AVATAR_CATALOGO.some((a) => a.id === u.avatar) ? u.avatar : null;
    estado = { cat: catDe(atual) || AVATAR_CATEGORIAS[0].id, atual, sel: atual, ocupado: false };
    if (dlg) dlg.remove();
    dlg = document.createElement("dialog");
    dlg.className = "av";
    dlg.setAttribute("aria-labelledby", "avTitulo");
    document.body.appendChild(dlg);
    dlg.addEventListener("close", () => { dlg.remove(); dlg = null; document.body.classList.remove("av-aberto"); });
    dlg.addEventListener("click", (e) => { if (e.target === dlg && !estado.ocupado) dlg.close(); });
    document.body.classList.add("av-aberto");
    desenharGaleria();
    dlg.showModal();
    const alvo = dlg.querySelector(".av__card.is-sel") || dlg.querySelector(".av__aba[aria-selected=true]");
    if (alvo) { alvo.focus({ preventScroll: true }); alvo.scrollIntoView({ block: "nearest" }); }
  }

  function desenharGaleria() {
    const u = Sessao.obter() || {};
    const sel = estado.sel;
    const abas = AVATAR_CATEGORIAS.map((c) => `<button type="button" role="tab" class="av__aba" data-cat="${c.id}" id="aba-${c.id}" aria-selected="${c.id === estado.cat}" aria-controls="avGrade" tabindex="${c.id === estado.cat ? 0 : -1}">${esc(c.nome)}</button>`).join("");
    const cards = AVATAR_CATALOGO.filter((a) => a.cat === estado.cat).map((a) =>
      `<button type="button" class="av__card${a.id === sel ? " is-sel" : ""}" data-id="${a.id}" aria-pressed="${a.id === sel}" title="${esc(a.nome)}">
         <span class="av__arte"><img src="assets/avatares/p/${a.id}.webp" alt="" loading="lazy" width="160" height="160"></span><span class="av__rotulo">${esc(a.nome)}</span>${a.id === estado.atual ? '<i class="av__atual">Atual</i>' : ""}<i class="av__marca" aria-hidden="true"></i></button>`).join("");
    const prevSrc = sel ? `assets/avatares/g/${sel}.webp` : avatarSrc(u, "g");
    const catSel = sel ? catDe(sel) : null;
    dlg.innerHTML = `<div class="av__caixa" role="document" data-tema="${estado.cat}"><i class="av__orn av__orn--a" aria-hidden="true"></i><i class="av__orn av__orn--b" aria-hidden="true"></i>
      <header class="av__topo"><h2 id="avTitulo"><small>Galeria de identidades</small>Escolha seu retrato</h2>
        <button type="button" class="av__fechar" aria-label="Fechar">${ic("circle-x")}</button></header>
      <div class="av__corpo">
        <section class="av__lado" aria-live="polite"><div class="av__moldura"${catSel ? ` data-tema="${catSel}"` : ""}><i class="av__gema" aria-hidden="true"></i><img id="avPrev" src="${esc(prevSrc)}" alt="Pré-visualização do retrato"></div>
          <p class="av__nome" id="avNome">${esc(sel ? nomeDe(sel) : (u.avatar === "personalizado" ? "Imagem personalizada" : "Nenhum selecionado"))}</p>
          <p class="av__msg" id="avMsg" role="status"></p></section>
        <section class="av__lista"><div class="av__abas" role="tablist" aria-label="Categorias de retratos">${abas}</div>
          <div class="av__grade" id="avGrade" role="tabpanel" aria-labelledby="aba-${estado.cat}">${cards}</div></section>
      </div>
      <footer class="av__rodape">
        <button type="button" class="av__upload" id="avUpload">${ic("camera")}<span>Enviar minha própria imagem</span></button>
        <div class="av__acoes"><button type="button" class="btn btn--fantasma" id="avCancelar">Cancelar</button>
          <button type="button" class="btn btn--primario" id="avConfirmar"${sel && sel !== estado.atual ? "" : " disabled"}>Confirmar</button></div>
      </footer></div>`;
    ligarGaleria();
  }

  function ligarGaleria() {
    const q = (s) => dlg.querySelector(s);
    q(".av__fechar").onclick = q("#avCancelar").onclick = () => dlg.close();
    const abas = [...dlg.querySelectorAll(".av__aba")];
    abas.forEach((b, i) => {
      b.onclick = () => { estado.cat = b.dataset.cat; desenharGaleria(); dlg.querySelector(".av__aba[aria-selected=true]").focus(); };
      b.onkeydown = (e) => {
        const d = e.key === "ArrowRight" ? 1 : e.key === "ArrowLeft" ? -1 : 0;
        if (d) { e.preventDefault(); abas[(i + d + abas.length) % abas.length].click(); }
      };
    });
    dlg.querySelectorAll(".av__card").forEach((b) => (b.onclick = () => {
      estado.sel = b.dataset.id;
      dlg.querySelectorAll(".av__card").forEach((c) => { const on = c === b; c.classList.toggle("is-sel", on); c.setAttribute("aria-pressed", on); });
      const prev = q("#avPrev"), mold = prev.closest(".av__moldura");
      mold.dataset.tema = catDe(estado.sel);
      prev.src = `assets/avatares/g/${estado.sel}.webp`;
      prev.classList.remove("av__troca"); void prev.offsetWidth; prev.classList.add("av__troca");
      q("#avNome").textContent = nomeDe(estado.sel);
      q("#avConfirmar").disabled = estado.sel === estado.atual;
    }));
    q("#avConfirmar").onclick = confirmarCatalogo;
    q("#avUpload").onclick = abrirUpload;
  }

  function msg(t, erro) { const m = dlg.querySelector("#avMsg"); if (m) { m.textContent = t || ""; m.classList.toggle("av__msg--erro", !!erro); } }
  function ocupar(sim) {
    estado.ocupado = sim;
    dlg.querySelectorAll("button").forEach((b) => { if (sim) { b.dataset.d = b.disabled ? "1" : ""; b.disabled = true; } else b.disabled = b.dataset.d === "1"; });
  }
  function aplicar(u) {
    const atual = Sessao.obter() || {};
    const { avatarImagem, ...resto } = atual;
    Sessao.salvar({ ...resto, ...u, token: atual.token });
    montarPerfilAtual();
  }

  async function confirmarCatalogo() {
    ocupar(true); msg("Salvando...");
    try { aplicar(await Api.escolherAvatar(estado.sel)); dlg.close(); }
    catch (e) { ocupar(false); msg(e.mensagem || "Não foi possível salvar. Tente novamente.", true); }
  }

  function abrirUpload() {
    dlg.innerHTML = `<div class="av__caixa av__caixa--corte" role="document">
      <header class="av__topo"><h2 id="avTitulo">Sua própria imagem</h2><button type="button" class="av__fechar" aria-label="Fechar">${ic("circle-x")}</button></header>
      <div class="av__corpo av__corpo--corte"><div class="av__escolha" id="avEscolha">
          <p>JPEG, PNG ou WebP, até 5 MB.</p>
          <label class="btn btn--fantasma av__arq">${ic("camera")}<span>Escolher arquivo</span><input class="sr" id="avArq" type="file" accept="image/jpeg,image/png,image/webp"></label>
          <p class="av__msg" id="avMsg" role="status"></p></div>
        <div class="av__corte" id="avCorte" hidden>
          <div class="av__visor" id="avVisor" tabindex="0" aria-label="Área de recorte. Arraste a imagem ou use as setas para reposicionar."><canvas id="avCanvas" width="${VISOR}" height="${VISOR}"></canvas><i class="av__mascara" aria-hidden="true"></i></div>
          <label class="av__zoom">${ic("search")}<span class="sr">Zoom</span><input type="range" id="avZoom" min="1" max="4" step="0.01" value="1" aria-label="Zoom"></label>
          <p class="av__msg" id="avMsg2" role="status"></p></div></div>
      <footer class="av__rodape"><button type="button" class="av__upload" id="avVoltar">‹ <span>Voltar à galeria</span></button>
        <div class="av__acoes"><button type="button" class="btn btn--primario" id="avSalvar" disabled>Usar esta imagem</button></div></footer></div>`;
    const q = (s) => dlg.querySelector(s);
    q(".av__fechar").onclick = () => dlg.close();
    q("#avVoltar").onclick = () => { if (!estado.ocupado) desenharGaleria(); };
    q("#avArq").onchange = (e) => carregar(e.target.files[0]);
    q("#avArq").focus();
  }

  function carregar(arq) {
    const erro = (t) => { const m = dlg.querySelector("#avMsg"); m.textContent = t; m.classList.add("av__msg--erro"); };
    if (!arq) return;
    if (!TIPOS.includes(arq.type)) return erro("Formato não suportado. Use JPEG, PNG ou WebP.");
    if (arq.size > MAX_ARQ) return erro("Arquivo grande demais (máximo 5 MB).");
    const url = URL.createObjectURL(arq), img = new Image();
    img.onerror = () => { URL.revokeObjectURL(url); erro("Não foi possível ler esta imagem."); };
    img.onload = () => {
      URL.revokeObjectURL(url);
      if (Math.min(img.naturalWidth, img.naturalHeight) < 64) return erro("Imagem pequena demais (mínimo 64 px).");
      iniciarCorte(img);
    };
    img.src = url;
  }

  function iniciarCorte(img) {
    const q = (s) => dlg.querySelector(s);
    q("#avEscolha").hidden = true; q("#avCorte").hidden = false;
    const cv = q("#avCanvas"), ctx = cv.getContext("2d"), zoom = q("#avZoom"), visor = q("#avVisor");
    const base = VISOR / Math.min(img.naturalWidth, img.naturalHeight);
    let k = 1, ox = (VISOR - img.naturalWidth * base) / 2, oy = (VISOR - img.naturalHeight * base) / 2;
    const lim = () => {
      const w = img.naturalWidth * base * k, h = img.naturalHeight * base * k;
      ox = Math.min(0, Math.max(VISOR - w, ox)); oy = Math.min(0, Math.max(VISOR - h, oy));
    };
    const desenhar = () => {
      lim(); ctx.fillStyle = "#000"; ctx.fillRect(0, 0, VISOR, VISOR);
      ctx.drawImage(img, ox, oy, img.naturalWidth * base * k, img.naturalHeight * base * k);
    };
    const zoomar = (novo) => {
      const cx = (VISOR / 2 - ox) / k, cy = (VISOR / 2 - oy) / k;
      k = novo; ox = VISOR / 2 - cx * k; oy = VISOR / 2 - cy * k; desenhar();
    };
    zoom.oninput = () => zoomar(parseFloat(zoom.value));
    let ptr = null;
    const escala = () => VISOR / cv.getBoundingClientRect().width;
    visor.onpointerdown = (e) => { ptr = { x: e.clientX, y: e.clientY }; visor.setPointerCapture(e.pointerId); visor.classList.add("is-arrastando"); };
    visor.onpointermove = (e) => { if (!ptr) return; const s = escala(); ox += (e.clientX - ptr.x) * s; oy += (e.clientY - ptr.y) * s; ptr = { x: e.clientX, y: e.clientY }; desenhar(); };
    visor.onpointerup = visor.onpointercancel = () => { ptr = null; visor.classList.remove("is-arrastando"); };
    visor.onkeydown = (e) => {
      const p = { ArrowLeft: [-12, 0], ArrowRight: [12, 0], ArrowUp: [0, -12], ArrowDown: [0, 12] }[e.key];
      if (p) { e.preventDefault(); ox += p[0]; oy += p[1]; desenhar(); }
      else if (e.key === "+" || e.key === "-") { zoom.value = Math.min(4, Math.max(1, k + (e.key === "+" ? .1 : -.1))); zoomar(parseFloat(zoom.value)); }
    };
    visor.onwheel = (e) => { e.preventDefault(); zoom.value = Math.min(4, Math.max(1, k - e.deltaY * .002)); zoomar(parseFloat(zoom.value)); };
    desenhar();
    const salvar = q("#avSalvar"); salvar.disabled = false;
    salvar.onclick = async () => {
      const saida = document.createElement("canvas"); saida.width = saida.height = SAIDA;
      const f = SAIDA / VISOR, c2 = saida.getContext("2d");
      c2.fillStyle = "#000"; c2.fillRect(0, 0, SAIDA, SAIDA);
      c2.drawImage(img, ox * f, oy * f, img.naturalWidth * base * k * f, img.naturalHeight * base * k * f);
      let dados = "", qual = .88;
      do { dados = saida.toDataURL("image/jpeg", qual); qual -= .12; } while (dados.length * 3 / 4 > MAX_SAIDA && qual > .3);
      ocupar(true); const m = q("#avMsg2"); m.textContent = "Salvando..."; m.classList.remove("av__msg--erro");
      try { aplicar(await Api.enviarAvatar(dados)); dlg.close(); }
      catch (e) { ocupar(false); m.textContent = e.mensagem || "Não foi possível salvar. Tente novamente."; m.classList.add("av__msg--erro"); }
    };
    visor.focus({ preventScroll: true });
  }

  return { abrir };
})();
