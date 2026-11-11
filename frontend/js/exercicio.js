const params = new URLSearchParams(window.location.search);
const idModulo = params.get("modulo");
const NIVEL = { FACIL: ["Fácil", 1], MEDIO: ["Médio", 2], DIFICIL: ["Difícil", 3] };
let exercicios = [];
let atual = 0;
let tituloModulo = "Módulo";
let temaModulo = "padrao";
const $ = (id) => document.getElementById(id);

function mostrarFeedback(ok, texto) {
  const f = $("feedback");
  f.hidden = false;
  f.className = "feedback " + (ok ? "ok" : "erro");
  f.innerHTML = ic(ok ? "circle-check" : "circle-x") + "<span>" + esc(texto) + "</span>";
}
function entrada() {
  const c = $("corpo");
  c.classList.remove("entra"); void c.offsetWidth; c.classList.add("entra");
}

function simuladorDo(ex, tema) {
  const t = (ex.descricao || "").toLowerCase();
  if (/avl|rota[cç]|balancead/.test(t)) return "avl";
  if (/bubble|bolha|adjacentes|subirem/.test(t)) return "bubble";
  if (/busca bin[áa]ria/.test(t)) return "busca";
  if (/hash|coli[sz]|encadeamento/.test(t)) return "hash";
  if (/[áa]rvore|\bbst\b|em-ordem|in-order|percurso/.test(t)) return "bst";
  if (tema === "hash") return "hash";
  if (tema === "arvore") return "bst";
  return null;
}
function painelPratica(ex) {
  const alvo = $("vernapratica"), sim = simuladorDo(ex, temaModulo);
  alvo.innerHTML = ""; alvo.hidden = !sim;
  if (!sim) return;
  const url = "laboratorio.html?sim=" + sim;
  alvo.innerHTML = `<div class="vernapratica__cab"><button class="btn btn--fantasma" id="btnPratica" type="button" aria-expanded="false">${ic("book-open")}<span>Ver na prática</span></button>
    <span class="dica">Execute este conceito passo a passo no Grimório Vivo antes de responder.</span></div><div id="praticaCorpo" hidden></div>`;
  $("btnPratica").addEventListener("click", () => {
    const corpo = $("praticaCorpo"), abrir = corpo.hidden;
    corpo.hidden = !abrir; $("btnPratica").setAttribute("aria-expanded", String(abrir));
    $("btnPratica").querySelector("span").textContent = abrir ? "Fechar simulação" : "Ver na prática";
    if (abrir && !corpo.firstChild) corpo.innerHTML = `<iframe title="Simulação no Grimório Vivo" src="${url}&embed=1" loading="lazy"></iframe><p class="dica"><a href="${url}">Abrir em tela cheia</a></p>`;
  });
}

function renderizarExercicio(ex) {
  const total = exercicios.length;
  $("feedback").hidden = true;
  $("btnProximo").hidden = true;
  $("btnConcluir").hidden = true;
  $("btnAnterior").hidden = atual === 0;
  $("btnProximo").textContent = "Próximo desafio";
  $("numero").textContent = "Desafio " + (atual + 1) + " de " + total;
  $("barra").style.width = (atual / total) * 100 + "%";
  $("trilha").setAttribute("aria-valuenow", atual);
  $("trilha").setAttribute("aria-valuemax", total);
  const [rotulo, n] = NIVEL[ex.dificuldade] || [ex.dificuldade, 1];
  $("dificuldade").className = "dificuldade " + esc(ex.dificuldade);
  $("dificuldade").innerHTML = `<span class="pips">${[1, 2, 3].map((k) => `<i class="${k <= n ? "on" : ""}"></i>`).join("")}</span>${esc(rotulo)}`;
  $("pontos").innerHTML = ic("star") + esc(ex.pontos) + " pts";
  $("descricao").textContent = ex.descricao;
  const fig = $("visual"), src = visualSrc(ex.visual);
  fig.hidden = !src;
  fig.innerHTML = src ? `<img src="${esc(src)}" alt="Ilustração do enunciado">` : "";
  $("opcoes").innerHTML = (ex.opcoes || []).map((o, i) =>
    `<button class="opcao" type="button" data-i="${i}" style="--k:${i}"><span class="opcao__letra">${String.fromCharCode(65 + i)}</span><span class="opcao__texto">${esc(o)}</span></button>`).join("");
  entrada();
  painelPratica(ex);
  document.querySelectorAll(".opcao").forEach((btn) =>
    btn.addEventListener("click", async () => {
      if (btn.classList.contains("errada")) return;
      try {
        const resultado = await Api.responder(ex.id, Number(btn.dataset.i));
        const correta = resultado && resultado.correta;
        if (resultado && resultado.usuario) Perfil.atualizarUsuario(resultado.usuario);
        btn.classList.add(correta ? "certa" : "errada");
        mostrarFeedback(correta, correta ? "Correto! " + (resultado.feedback || "") : "Tente novamente.");
        if (correta) {
          document.querySelectorAll(".opcao").forEach((b) => (b.disabled = true));
          $("barra").style.width = ((atual + 1) / total) * 100 + "%";
          if (atual < total - 1) $("btnProximo").hidden = false; else $("btnConcluir").hidden = false;
        }
      } catch (e) {
        mostrarFeedback(false, "Não foi possível enviar a resposta.");
      }
    }));
}

async function iniciar() {
  try {
    const modulo = await Api.abrirModulo(idModulo);
    if (modulo && modulo.titulo) tituloModulo = modulo.titulo;
    const tema = temaModulo = temaDoModulo(modulo);
    $("moduloNome").textContent = tituloModulo;
    $("moduloSelo").innerHTML = `<span class="hex">${ic(TEMAS[tema].icone)}</span>`;
    $("desafio").style.setProperty("--tom", TEMAS[tema].tom);
    exercicios = Array.isArray(modulo) ? modulo : (modulo && modulo.exercicios) || [];
    if (exercicios.length === 0) { $("descricao").textContent = "Este módulo ainda não tem exercícios."; return; }
    renderizarExercicio(exercicios[atual]);
  } catch (e) {
    $("descricao").textContent = "Erro ao carregar o exercício.";
  }
}
$("btnProximo").addEventListener("click", () => { atual++; renderizarExercicio(exercicios[atual]); });
$("btnAnterior").addEventListener("click", () => { atual--; renderizarExercicio(exercicios[atual]); });
iniciar();
