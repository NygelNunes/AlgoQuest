const API_URL = window.AQ_API_URL || "http://" + (location.hostname || "localhost") + ":8080/api";

function tokenAtual() {
  try { return (JSON.parse(sessionStorage.getItem("aq_usuario")) || {}).token || null; } catch (e) { return null; }
}

async function apiFetch(caminho, opcoes = {}) {
  const cabecalhos = { "Content-Type": "application/json" };
  const token = tokenAtual();
  if (token) cabecalhos["Authorization"] = "Bearer " + token;
  const resposta = await fetch(API_URL + caminho, { ...opcoes, headers: cabecalhos });
  if (resposta.status === 401 && caminho !== "/auth/login") {
    sessionStorage.removeItem("aq_usuario");
    if (!/index\.html$|\/$/.test(location.pathname)) location.href = "index.html";
  }
  if (!resposta.ok) {
    const e = new Error("Erro " + resposta.status);
    e.status = resposta.status;
    try { e.mensagem = (await resposta.json()).erro; } catch (x) {  }
    throw e;
  }
  const texto = await resposta.text();
  return texto ? JSON.parse(texto) : null;
}

const Api = {
  login: (nomeUsuario, senha) =>
    apiFetch("/auth/login", { method: "POST", body: JSON.stringify({ nomeUsuario, senha }) }),
  registrar: (nomeUsuario, senha) =>
    apiFetch("/auth/registrar", { method: "POST", body: JSON.stringify({ nomeUsuario, senha }) }),
  escolherAvatar: (avatar) =>
    apiFetch("/auth/me/avatar", { method: "POST", body: JSON.stringify({ avatar }) }),
  enviarAvatar: (imagem) =>
    apiFetch("/auth/me/avatar/personalizado", { method: "POST", body: JSON.stringify({ imagem }) }),
  admModulos: () => apiFetch("/admin/modulos"),
  admCriarModulo: (corpo) => apiFetch("/admin/modulos", { method: "POST", body: JSON.stringify(corpo) }),
  admEditarModulo: (id, corpo) => apiFetch("/admin/modulos/" + id, { method: "PUT", body: JSON.stringify(corpo) }),
  admExcluirModulo: (id) => apiFetch("/admin/modulos/" + id, { method: "DELETE" }),
  admMoverModulo: (id, direcao) => apiFetch("/admin/modulos/" + id + "/mover", { method: "POST", body: JSON.stringify({ direcao }) }),
  admQuestoes: (idModulo) => apiFetch("/admin/modulos/" + idModulo + "/questoes"),
  admCriarQuestao: (idModulo, corpo) => apiFetch("/admin/modulos/" + idModulo + "/questoes", { method: "POST", body: JSON.stringify(corpo) }),
  admEditarQuestao: (id, corpo) => apiFetch("/admin/questoes/" + id, { method: "PUT", body: JSON.stringify(corpo) }),
  admExcluirQuestao: (id) => apiFetch("/admin/questoes/" + id, { method: "DELETE" }),
  admMoverQuestao: (id, direcao) => apiFetch("/admin/questoes/" + id + "/mover", { method: "POST", body: JSON.stringify({ direcao }) }),
  admImportar: (idModulo, corpo) => apiFetch("/admin/modulos/" + idModulo + "/importacao", { method: "POST", body: JSON.stringify(corpo) }),
  admUsuarios: () => apiFetch("/admin/usuarios"),
  admPapel: (id, papel) => apiFetch("/admin/usuarios/" + id + "/papel", { method: "PUT", body: JSON.stringify({ papel }) }),
  logout: () => apiFetch("/auth/logout", { method: "POST" }),
  eu: () => apiFetch("/auth/me"),
  listarModulos: () => apiFetch("/modulos"),
  abrirModulo: (id) => apiFetch("/modulos/" + id),
  responder: (idExercicio, resposta) =>
    apiFetch("/modulos/exercicios/" + idExercicio + "/resposta", {
      method: "POST",
      body: JSON.stringify({ resposta }),
    }),
};
