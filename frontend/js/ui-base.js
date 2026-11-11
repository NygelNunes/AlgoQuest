const Sessao = {
  salvar(u) { sessionStorage.setItem("aq_usuario", JSON.stringify(u)); },
  obter() { try { return JSON.parse(sessionStorage.getItem("aq_usuario")); } catch (e) { return null; } },
  limpar() { sessionStorage.removeItem("aq_usuario"); },
};
