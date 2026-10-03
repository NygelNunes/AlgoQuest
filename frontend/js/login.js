document.getElementById("btnEntrar").addEventListener("click", async () => {
  const usuario = document.getElementById("usuario").value;
  const senha = document.getElementById("senha").value;
  try {
    await Api.login(usuario, senha);
    window.location.href = "modulos.html";
  } catch (e) {
    document.getElementById("erro").textContent = "Não foi possível entrar.";
  }
});
