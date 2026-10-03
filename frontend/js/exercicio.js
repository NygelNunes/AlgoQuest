// TODO: buscar o exercicio atual via Api.abrirModulo(idModulo) e renderizar.
const params = new URLSearchParams(window.location.search);
const idModulo = params.get("modulo");

function renderizarExercicio(ex) {
  document.getElementById("dificuldade").textContent = ex.dificuldade + " · " + ex.pontos + " pts";
  document.getElementById("descricao").textContent = ex.descricao;
  document.getElementById("opcoes").innerHTML = ex.opcoes
    .map((o, i) => `<button class="opcao" data-i="${i}">${o}</button>`).join("");
  document.querySelectorAll(".opcao").forEach((btn) =>
    btn.addEventListener("click", async () => {
      const resultado = await Api.responder(ex.id, Number(btn.dataset.i));
      document.getElementById("feedback").textContent =
        resultado && resultado.correta ? "Correto! " + (resultado.feedback || "") : "Tente novamente.";
    }));
}
