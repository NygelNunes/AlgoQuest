const $ = (id) => document.getElementById(id);
const form = $("formLogin"), campoUsuario = $("usuario"), campoSenha = $("senha"), botao = $("btnEntrar");

function marcarErro(campo, texto) {
  const caixa = campo.closest(".campo");
  caixa.classList.toggle("campo--erro", !!texto);
  campo.setAttribute("aria-invalid", texto ? "true" : "false");
  caixa.querySelector(".campo__erro").textContent = texto || "";
}
function mostrarAlerta(texto) {
  const a = $("erro");
  a.hidden = !texto;
  a.innerHTML = texto ? '<i class="ic i-triangle-alert" aria-hidden="true"></i><span></span>' : "";
  if (texto) a.querySelector("span").textContent = texto;
}
let modoCadastro = false;
function rotuloBotao() { return modoCadastro ? "Criar conta" : "Entrar"; }
function trocarModo() {
  modoCadastro = !modoCadastro;
  $("tituloLogin").textContent = modoCadastro ? "Criar conta" : "Entrar no Reino";
  form.querySelector(".portal__topo p").textContent = modoCadastro ? "Escolha um nome de aventureiro e uma senha (mín. 8 caracteres)." : "Retome sua jornada de onde parou.";
  botao.querySelector(".btn__rotulo").textContent = rotuloBotao();
  campoSenha.autocomplete = modoCadastro ? "new-password" : "current-password";
  $("notaModo").textContent = modoCadastro ? "Já tem conta?" : "Ainda não tem conta?";
  $("btnModo").textContent = modoCadastro ? "Entrar" : "Criar conta";
  mostrarAlerta(""); marcarErro(campoUsuario, ""); marcarErro(campoSenha, "");
}
$("btnModo").addEventListener("click", trocarModo);
function ocupado(sim) {
  botao.disabled = sim;
  botao.classList.toggle("is-carregando", sim);
  botao.querySelector(".btn__rotulo").textContent = sim ? (modoCadastro ? "Criando..." : "Entrando...") : rotuloBotao();
  form.setAttribute("aria-busy", String(sim));
  campoUsuario.readOnly = campoSenha.readOnly = sim;
}
function sacudir() {
  form.classList.remove("portal--recusa"); void form.offsetWidth; form.classList.add("portal--recusa");
}

async function entrar(ev) {
  ev.preventDefault();
  mostrarAlerta("");
  const usuario = campoUsuario.value.trim(), senha = campoSenha.value;
  marcarErro(campoUsuario, usuario ? "" : "Informe seu usuário.");
  marcarErro(campoSenha, senha ? "" : "Informe sua senha.");
  if (!usuario || !senha) { sacudir(); (usuario ? campoSenha : campoUsuario).focus(); return; }
  if (modoCadastro && senha.length < 8) { marcarErro(campoSenha, "Use ao menos 8 caracteres."); sacudir(); campoSenha.focus(); return; }
  ocupado(true);
  try {
    const dados = modoCadastro ? await Api.registrar(usuario, senha) : await Api.login(usuario, senha);
    const { senha: _descartada, ...publico } = dados || {};
    Sessao.salvar({ nomeUsuario: usuario, ...publico });
    botao.classList.add("is-ok");
    window.location.href = "modulos.html";
  } catch (e) {
    const recusado = e && /^Erro \d+/.test(e.message);
    if (modoCadastro && recusado && e.mensagem) { mostrarAlerta(e.mensagem); ocupado(false); sacudir(); return; }
    mostrarAlerta(recusado ? "Usuário ou senha incorretos. Confira os dados e tente de novo."
                           : "Não foi possível falar com o servidor. Verifique se o backend está em execução.");
    ocupado(false); sacudir();
    if (recusado) { campoSenha.select(); }
  }
}
form.addEventListener("submit", entrar);
[campoUsuario, campoSenha].forEach((c) => c.addEventListener("input", () => { marcarErro(c, ""); mostrarAlerta(""); }));

$("btnOlho").addEventListener("click", (ev) => {
  const b = ev.currentTarget, mostrar = campoSenha.type === "password";
  campoSenha.type = mostrar ? "text" : "password";
  b.setAttribute("aria-pressed", String(mostrar));
  b.setAttribute("aria-label", mostrar ? "Ocultar senha" : "Mostrar senha");
  b.querySelector(".ic").className = "ic " + (mostrar ? "i-eye-off" : "i-eye");
  campoSenha.focus();
});
const caps = (e) => { if (e.getModifierState) $("avisoCaps").hidden = !e.getModifierState("CapsLock"); };
campoSenha.addEventListener("keydown", caps); campoSenha.addEventListener("keyup", caps);
campoSenha.addEventListener("blur", () => { $("avisoCaps").hidden = true; });

if (!matchMedia("(prefers-reduced-motion: reduce)").matches) {
  const b = $("brasas");
  for (let i = 0; i < 16; i++) {
    const s = document.createElement("i");
    s.style.cssText = `left:${(Math.random() * 100).toFixed(1)}%;--d:${(9 + Math.random() * 10).toFixed(1)}s;--a:${(-Math.random() * 14).toFixed(1)}s;--x:${(Math.random() * 60 - 30).toFixed(0)}px;--t:${(2 + Math.random() * 2.5).toFixed(1)}px`;
    b.appendChild(s);
  }
}
campoUsuario.focus({ preventScroll: true });
