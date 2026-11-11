const Tema = (() => {
  const raiz = document.documentElement;
  function categoria(u) {
    if (!u || !u.avatar || u.avatar === "personalizado") return null;
    const a = (typeof AVATAR_CATALOGO !== "undefined" ? AVATAR_CATALOGO : []).find((x) => x.id === u.avatar);
    return a ? a.cat : null;
  }
  function aplicar(u) {
    const c = categoria(u);
    if (c) raiz.dataset.tema = c; else delete raiz.dataset.tema;
    return c;
  }
  try { aplicar(JSON.parse(sessionStorage.getItem("aq_usuario"))); } catch (e) { aplicar(null); }
  return { categoria, aplicar };
})();
