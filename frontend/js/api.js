// Camada unica de comunicacao com o backend (Spring Boot).
const API_URL = "http://localhost:8080/api";

async function apiFetch(caminho, opcoes = {}) {
  const resposta = await fetch(API_URL + caminho, {
    headers: { "Content-Type": "application/json" },
    ...opcoes,
  });
  if (!resposta.ok) throw new Error("Erro " + resposta.status);
  const texto = await resposta.text();
  return texto ? JSON.parse(texto) : null;
}

const Api = {
  login: (nomeUsuario, senha) =>
    apiFetch("/auth/login", { method: "POST", body: JSON.stringify({ nomeUsuario, senha }) }),
  logout: () => apiFetch("/auth/logout", { method: "POST" }),
  listarModulos: () => apiFetch("/modulos"),
  abrirModulo: (id) => apiFetch("/modulos/" + id),
  responder: (idExercicio, resposta) =>
    apiFetch("/modulos/exercicios/" + idExercicio + "/resposta", {
      method: "POST",
      body: JSON.stringify({ resposta }),
    }),
};
