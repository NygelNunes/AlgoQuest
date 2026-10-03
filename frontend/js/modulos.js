// TODO: preencher nivel/xp com os dados do usuario logado.
async function carregarModulos() {
  const lista = document.getElementById("listaModulos");
  try {
    const modulos = (await Api.listarModulos()) || [];
    if (modulos.length === 0) {
      lista.innerHTML = '<p class="muted">Nenhum módulo disponível ainda.</p>';
      return;
    }
    lista.innerHTML = modulos.map((m) => `
      <div class="card">
        <h3>${m.titulo}</h3>
        <p class="muted">${m.descricao}</p>
        <div class="bar"><div style="width:${m.porcentagem || 0}%"></div></div>
        <a href="exercicio.html?modulo=${m.id}"><button>Estudar</button></a>
      </div>`).join("");
  } catch (e) {
    lista.innerHTML = '<p class="muted">Erro ao carregar módulos.</p>';
  }
}
carregarModulos();
