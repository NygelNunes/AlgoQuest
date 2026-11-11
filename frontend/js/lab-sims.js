(function (raiz) {
  "use strict";

  const clonar = (t) => (t ? { v: t.v, l: clonar(t.l), r: clonar(t.r) } : null);
  const alt = (t) => (t ? 1 + Math.max(alt(t.l), alt(t.r)) : 0);
  const fb = (t) => (t ? alt(t.l) - alt(t.r) : 0);
  const rotD = (y) => { const x = y.l; y.l = x.r; x.r = y; return x; };
  const rotE = (x) => { const y = x.r; x.r = y.l; y.l = x; return y; };
  const sorteio = (qtd, min, max) => {
    const s = new Set();
    while (s.size < qtd) s.add(min + Math.floor(Math.random() * (max - min + 1)));
    return [...s];
  };

  function numeros(txt, { min = 1, max = 99, qtd = 12, minQtd = 2, unicos = false } = {}) {
    const partes = String(txt || "").split(/[\s,;]+/).filter(Boolean);
    if (partes.length < minQtd) return { erro: "Informe pelo menos " + minQtd + " valores separados por vírgula." };
    if (partes.length > qtd) return { erro: "Use no máximo " + qtd + " valores para a visualização ficar legível." };
    const v = partes.map(Number);
    if (v.some((n) => !Number.isInteger(n) || n < min || n > max)) return { erro: "Use apenas números inteiros entre " + min + " e " + max + "." };
    if (unicos && new Set(v).size !== v.length) return { erro: "Não repita valores nesta simulação." };
    return { valores: v };
  }

  function gerarBubble({ valores }) {
    const n = valores.length, passos = [];
    let a = valores.map((v, i) => ({ id: i, v }));
    const snap = () => a.map((x) => ({ ...x }));
    passos.push({ a: snap(), ord: n, cmp: null, txt: "Vetor inicial. O Bubble Sort compara pares vizinhos e troca os que estão fora de ordem; a cada passada, o maior valor restante \"sobe\" até o fim." });
    let trocouAlgum = true;
    for (let p = 0; p < n - 1 && trocouAlgum; p++) {
      trocouAlgum = false;
      for (let j = 0; j < n - 1 - p; j++) {
        const x = a[j], y = a[j + 1], troca = x.v > y.v;
        passos.push({ a: snap(), ord: n - p, cmp: [j, j + 1], txt: "Passada " + (p + 1) + ": compare " + x.v + " (posição " + j + ") com " + y.v + " (posição " + (j + 1) + ")." });
        const pergunta = {
          enunciado: "Comparando " + x.v + " e " + y.v + ": o que o Bubble Sort faz?",
          opcoes: ["Troca os dois de lugar", "Mantém como está"], certa: troca ? 0 : 1,
          explica: troca ? x.v + " é maior que " + y.v + ", então ficariam fora de ordem: troca." : x.v + " não é maior que " + y.v + ": já estão em ordem, nada a fazer.",
        };
        if (troca) { a[j] = y; a[j + 1] = x; trocouAlgum = true; }
        passos.push({ a: snap(), ord: n - p, cmp: [j, j + 1], troca, pergunta,
          txt: troca ? "Troca feita: " + x.v + " avançou uma casa para a direita." : "Sem troca: " + x.v + " ≤ " + y.v + "." });
      }
      if (trocouAlgum && p < n - 2) {
        passos.push({ a: snap(), ord: n - 1 - p, cmp: null, txt: "Fim da passada " + (p + 1) + ": " + a[n - 1 - p].v + " chegou à posição final e não será mais comparado." });
      } else if (!trocouAlgum) {
        passos.push({ a: snap(), ord: 0, cmp: null, txt: "A passada " + (p + 1) + " não fez nenhuma troca: o vetor já está ordenado e o algoritmo pode parar (otimização que torna o melhor caso O(n))." });
      }
    }
    passos.push({ a: snap(), ord: 0, cmp: null, fim: true, txt: "Vetor ordenado: " + a.map((x) => x.v).join(", ") + ". No pior caso o Bubble Sort faz cerca de n²/2 comparações: O(n²)." });
    return passos;
  }

  function gerarBusca({ valores, alvo }) {
    const a = [...valores].sort((x, y) => x - y), passos = [], n = a.length;
    let lo = 0, hi = n - 1, comps = 0;
    passos.push({ a, lo, hi, mid: null, txt: "Lista ordenada com " + n + " elementos. Procuramos o valor " + alvo + ". A busca binária só funciona porque a lista está ordenada." });
    while (lo <= hi) {
      const mid = Math.floor((lo + hi) / 2); comps++;
      passos.push({ a, lo, hi, mid, txt: "Intervalo [" + lo + ".." + hi + "]: o meio é o índice ⌊(" + lo + "+" + hi + ")/2⌋ = " + mid + ", valor " + a[mid] + ". Compare com o alvo " + alvo + "." });
      const cmp = alvo === a[mid] ? 0 : alvo < a[mid] ? 1 : 2;
      const pergunta = {
        enunciado: "O meio vale " + a[mid] + " e o alvo é " + alvo + ". O que fazer?",
        opcoes: ["Encontrou: encerrar", "Descartar a metade direita (seguir pela esquerda)", "Descartar a metade esquerda (seguir pela direita)"], certa: cmp,
        explica: cmp === 0 ? "Os valores são iguais: achamos o alvo." : cmp === 1 ? alvo + " < " + a[mid] + ": se existir, está à esquerda do meio." : alvo + " > " + a[mid] + ": se existir, está à direita do meio.",
      };
      if (cmp === 0) {
        passos.push({ a, lo, hi, mid, achou: mid, pergunta, txt: "Encontrado no índice " + mid + " após " + comps + " comparação(ões). Uma busca sequencial poderia precisar de até " + n + "." });
        return passos;
      }
      if (cmp === 1) hi = mid - 1; else lo = mid + 1;
      passos.push({ a, lo, hi, mid, pergunta, descartou: cmp === 1 ? "dir" : "esq",
        txt: cmp === 1 ? alvo + " é menor que " + a[mid] + ": descartamos o meio e tudo à direita. Novo intervalo: " + (lo <= hi ? "[" + lo + ".." + hi + "]" : "vazio") + "."
          : alvo + " é maior que " + a[mid] + ": descartamos o meio e tudo à esquerda. Novo intervalo: " + (lo <= hi ? "[" + lo + ".." + hi + "]" : "vazio") + "." });
    }
    passos.push({ a, lo, hi, mid: null, fim: true, txt: "O intervalo ficou vazio: " + alvo + " não está na lista (" + comps + " comparações). O custo cresce como log₂ n: O(log n)." });
    return passos;
  }

  function gerarHash({ valores, m }) {
    const tab = Array.from({ length: m }, () => []), passos = [];
    const snap = () => tab.map((c) => [...c]);
    passos.push({ tab: snap(), m, pos: null, txt: "Tabela vazia com " + m + " posições. Usaremos a função h(k) = k mod " + m + " e encadeamento: chaves que caem na mesma posição formam uma lista." });
    let colisoes = 0;
    for (const k of valores) {
      const r = k % m, ocupada = tab[r].length > 0;
      passos.push({ tab: snap(), m, pos: null, chave: k, txt: "Próxima chave a inserir: " + k + ". Calcule h(" + k + ") = " + k + " mod " + m + "." });
      const opc = [...new Set([r, (r + 1) % m, (r + 2) % m, (r + m - 1) % m])].sort((x, y) => x - y);
      const antes = [...tab[r]];
      tab[r].push(k);
      if (ocupada) colisoes++;
      passos.push({ tab: snap(), m, pos: r, chave: k, colisao: ocupada,
        pergunta: { enunciado: "Em qual posição cai a chave " + k + " com h(k) = k mod " + m + "?", opcoes: opc.map((x) => "Posição " + x), certa: opc.indexOf(r),
          explica: k + " mod " + m + " = " + r + " (o resto da divisão de " + k + " por " + m + ")." },
        txt: k + " mod " + m + " = " + r + ". " + (ocupada ? "Colisão! A posição " + r + " já guarda " + antes.join(" → ") + ": " + k + " entra no fim dessa lista." : "A posição " + r + " está livre: " + k + " é guardado ali.") });
    }
    const maior = Math.max(...tab.map((c) => c.length));
    passos.push({ tab: snap(), m, pos: null, fim: true, txt: "Inserção concluída: " + valores.length + " chaves em " + m + " posições (fator de carga " + (valores.length / m).toFixed(2) + "), " + colisoes + " colisão(ões), maior lista com " + maior + " chave(s). Se todas colidissem, a busca viraria O(n); com boa distribuição, é O(1) em média." });
    return passos;
  }

  function gerarBST({ valores }) {
    let raiz = null; const passos = [];
    const snap = (extra) => ({ arvore: clonar(raiz), ...extra });
    passos.push(snap({ txt: "Árvore vazia. Regra da árvore binária de busca: à esquerda de um nó ficam valores menores; à direita, maiores." }));
    for (const v of valores) {
      if (!raiz) { raiz = { v, l: null, r: null }; passos.push(snap({ novo: v, txt: v + " é o primeiro valor: vira a raiz." })); continue; }
      let no = raiz; const caminho = [];
      for (;;) {
        caminho.push(no.v);
        passos.push(snap({ atual: no.v, caminho: [...caminho], txt: "Inserindo " + v + ": compare com o nó " + no.v + "." }));
        const esq = v < no.v;
        passos.push(snap({ atual: no.v, caminho: [...caminho],
          pergunta: { enunciado: "Inserindo " + v + ", nó atual " + no.v + ". Para onde seguir?", opcoes: ["Esquerda", "Direita"], certa: esq ? 0 : 1,
            explica: v + (esq ? " < " : " > ") + no.v + ": " + (esq ? "valores menores ficam à esquerda." : "valores maiores ficam à direita.") },
          txt: v + (esq ? " é menor" : " é maior") + " que " + no.v + ": seguir para a " + (esq ? "esquerda." : "direita.") }));
        const prox = esq ? no.l : no.r;
        if (!prox) {
          const novo = { v, l: null, r: null };
          if (esq) no.l = novo; else no.r = novo;
          passos.push(snap({ novo: v, caminho: [...caminho], txt: "Posição vazia encontrada: " + v + " vira filho " + (esq ? "esquerdo" : "direito") + " de " + no.v + "." }));
          break;
        }
        no = prox;
      }
    }
    const emOrdem = [...valores].sort((x, y) => x - y);
    passos.push(snap({ fim: true, txt: "Árvore pronta (altura " + alt(raiz) + "). O percurso em ordem lê as chaves ordenadas: " + emOrdem.join(", ") + ". Cada busca desce um caminho: O(altura)." }));
    return passos;
  }

  const ROT = ["Rotação simples à direita", "Rotação simples à esquerda", "Rotação dupla esquerda-direita", "Rotação dupla direita-esquerda"];
  const CASO = { LL: 0, RR: 1, LR: 2, RL: 3 };
  const EXPLICA = {
    LL: "Fator +2 e o filho esquerdo pende para a esquerda (caso esquerda-esquerda): uma rotação simples à direita resolve.",
    RR: "Fator −2 e o filho direito pende para a direita (caso direita-direita): uma rotação simples à esquerda resolve.",
    LR: "Fator +2, mas o filho esquerdo pende para a direita (caso esquerda-direita): primeiro gira-se o filho à esquerda, depois o nó à direita.",
    RL: "Fator −2, mas o filho direito pende para a esquerda (caso direita-esquerda): primeiro gira-se o filho à direita, depois o nó à esquerda.",
  };

  function gerarAVL({ valores }) {
    let raiz = null; const passos = [];
    const snap = (extra) => ({ arvore: clonar(raiz), fb: true, ...extra });
    passos.push(snap({ txt: "Árvore AVL vazia. Regra extra: em todo nó, a diferença entre as alturas das subárvores (fator de balanceamento) deve ficar entre −1 e +1. Se passar disso, giramos." }));
    for (const v of valores) {
      if (!raiz) { raiz = { v, l: null, r: null }; passos.push(snap({ novo: v, txt: v + " é a primeira chave: vira a raiz (fator 0)." })); continue; }
      let no = raiz; const path = [];
      for (;;) {
        path.push(no);
        const cam = path.map((n) => n.v);
        passos.push(snap({ atual: no.v, caminho: cam, txt: "Inserindo " + v + ": compare com o nó " + no.v + "." }));
        const esq = v < no.v;
        passos.push(snap({ atual: no.v, caminho: cam,
          pergunta: { enunciado: "Inserindo " + v + ", nó atual " + no.v + ". Para onde seguir?", opcoes: ["Esquerda", "Direita"], certa: esq ? 0 : 1,
            explica: v + (esq ? " < " : " > ") + no.v + ": " + (esq ? "menores à esquerda." : "maiores à direita.") },
          txt: v + (esq ? " é menor" : " é maior") + " que " + no.v + ": seguir para a " + (esq ? "esquerda." : "direita.") }));
        const prox = esq ? no.l : no.r;
        if (!prox) {
          const novo = { v, l: null, r: null };
          if (esq) no.l = novo; else no.r = novo;
          passos.push(snap({ novo: v, caminho: cam, txt: v + " entra como filho " + (esq ? "esquerdo" : "direito") + " de " + no.v + ". Agora voltamos pelo caminho recalculando os fatores de balanceamento." }));
          break;
        }
        no = prox;
      }
      let girou = false;
      for (let k = path.length - 1; k >= 0 && !girou; k--) {
        const n = path[k], f = fb(n), he = alt(n.l), hd = alt(n.r);
        if (Math.abs(f) <= 1) {
          passos.push(snap({ atual: n.v, txt: "Subindo: nó " + n.v + " tem altura esquerda " + he + " e direita " + hd + " → fator " + (f > 0 ? "+" : "") + f + ". Equilibrado." }));
          continue;
        }
        passos.push(snap({ ruim: n.v, txt: "Subindo: nó " + n.v + " tem altura esquerda " + he + " e direita " + hd + " → fator " + (f > 0 ? "+" : "") + f + ". Desbalanceado! Precisa de rotação." }));
        const filho = f > 1 ? n.l : n.r;
        const caso = f > 1 ? (fb(filho) >= 0 ? "LL" : "LR") : (fb(filho) <= 0 ? "RR" : "RL");
        const pai = k > 0 ? path[k - 1] : null;
        const prender = (sub) => { if (!pai) raiz = sub; else if (pai.l === n) pai.l = sub; else pai.r = sub; };
        const pergunta = { enunciado: "O nó " + n.v + " ficou com fator " + (f > 0 ? "+" : "") + f + " e seu filho " + filho.v + " tem fator " + (fb(filho) > 0 ? "+" : "") + fb(filho) + ". Qual rotação corrige?",
          opcoes: ROT, certa: CASO[caso], explica: EXPLICA[caso] };
        const nv = n.v, fv = filho.v;
        if (caso === "LL") {
          const sub = rotD(n); prender(sub);
          passos.push(snap({ girou: sub.v, pergunta, txt: ROT[0] + " em " + nv + ": o filho " + fv + " sobe para o lugar de " + nv + ", e " + nv + " desce para a direita." }));
        } else if (caso === "RR") {
          const sub = rotE(n); prender(sub);
          passos.push(snap({ girou: sub.v, pergunta, txt: ROT[1] + " em " + nv + ": o filho " + fv + " sobe para o lugar de " + nv + ", e " + nv + " desce para a esquerda." }));
        } else if (caso === "LR") {
          n.l = rotE(n.l);
          passos.push(snap({ ruim: nv, girou: n.l.v, pergunta, txt: "1ª etapa da " + ROT[2].toLowerCase() + ": rotação à esquerda em " + fv + ". O neto " + n.l.v + " sobe e o caso vira esquerda-esquerda." }));
          const sub = rotD(n); prender(sub);
          passos.push(snap({ girou: sub.v, txt: "2ª etapa: rotação à direita em " + nv + ". " + sub.v + " vira a raiz desta subárvore." }));
        } else {
          n.r = rotD(n.r);
          passos.push(snap({ ruim: nv, girou: n.r.v, pergunta, txt: "1ª etapa da " + ROT[3].toLowerCase() + ": rotação à direita em " + fv + ". O neto " + n.r.v + " sobe e o caso vira direita-direita." }));
          const sub = rotE(n); prender(sub);
          passos.push(snap({ girou: sub.v, txt: "2ª etapa: rotação à esquerda em " + nv + ". " + sub.v + " vira a raiz desta subárvore." }));
        }
        girou = true;
        passos.push(snap({ txt: "Árvore rebalanceada: uma rotação (simples ou dupla) basta por inserção, e a altura da subárvore volta ao que era antes de inserir " + v + "." }));
      }
    }
    passos.push(snap({ fim: true, txt: "AVL concluída com altura " + alt(raiz) + " para " + valores.length + " chaves. A altura nunca passa de ~1,44·log₂(n), então busca, inserção e remoção são O(log n) — sem degenerar em lista como a BST." }));
    return passos;
  }

  const SIMS = {
    bubble: { titulo: "Bubble Sort", icone: "arrow-down-up", tom: "#d0343e",
      resumo: "Veja os vizinhos serem comparados e trocados até o caos virar ordem.",
      fato: "Pior caso O(n²) · melhor caso O(n) com parada antecipada · estável.",
      campos: [{ id: "v", rot: "Valores (2 a 10, inteiros de 1 a 99)", padrao: "5, 2, 9, 1, 6, 3" }],
      aleatorio: () => ({ v: sorteio(7, 1, 99).join(", ") }),
      ler: (f) => { const r = numeros(f.v, { qtd: 10 }); return r.erro ? r : { entrada: { valores: r.valores } }; },
      gerar: gerarBubble, manual: true },
    busca: { titulo: "Busca Binária", icone: "search", tom: "#b89a5e",
      resumo: "A cada passo metade do reino é descartada. Acompanhe o intervalo encolher.",
      fato: "Exige lista ordenada · O(log n): 1 milhão de itens em ~20 comparações.",
      campos: [{ id: "v", rot: "Valores (2 a 16; serão ordenados)", padrao: "3, 8, 12, 17, 21, 26, 33, 40, 47, 55, 62, 71" }, { id: "alvo", rot: "Valor procurado", padrao: "47", curto: true }],
      aleatorio: () => { const v = sorteio(12, 1, 99).sort((a, b) => a - b); return { v: v.join(", "), alvo: String(v[Math.floor(Math.random() * v.length)]) }; },
      ler: (f) => { const r = numeros(f.v, { qtd: 16, unicos: true }); if (r.erro) return r; const alvo = Number(f.alvo);
        if (!Number.isInteger(alvo) || alvo < 1 || alvo > 99) return { erro: "O valor procurado deve ser um inteiro entre 1 e 99." };
        return { entrada: { valores: r.valores, alvo } }; },
      gerar: gerarBusca },
    hash: { titulo: "Tabela Hash", icone: "hash", tom: "#6f8fb5",
      resumo: "Chaves viram posições por uma função de espalhamento; colisões formam listas.",
      fato: "Média O(1) com boa função e fator de carga baixo · pior caso O(n).",
      campos: [{ id: "v", rot: "Chaves (2 a 12, inteiros de 1 a 99)", padrao: "15, 22, 9, 36, 12, 25, 41" }, { id: "m", rot: "Posições (3 a 13)", padrao: "7", curto: true }],
      aleatorio: () => ({ v: sorteio(8, 1, 99).join(", "), m: String(5 + Math.floor(Math.random() * 4)) }),
      ler: (f) => { const r = numeros(f.v, { qtd: 12, unicos: true }); if (r.erro) return r; const m = Number(f.m);
        if (!Number.isInteger(m) || m < 3 || m > 13) return { erro: "O número de posições deve ser um inteiro entre 3 e 13." };
        return { entrada: { valores: r.valores, m } }; },
      gerar: gerarHash },
    bst: { titulo: "Árvore Binária de Busca", icone: "network", tom: "#3f9a5a",
      resumo: "Insira valores e acompanhe o caminho da raiz até o lugar de cada um.",
      fato: "Busca e inserção em O(altura): O(log n) se equilibrada, O(n) se degenerar.",
      campos: [{ id: "v", rot: "Valores (2 a 12, sem repetir)", padrao: "50, 30, 70, 20, 40, 60, 80" }],
      aleatorio: () => ({ v: sorteio(8, 1, 99).join(", ") }),
      ler: (f) => { const r = numeros(f.v, { qtd: 12, unicos: true }); return r.erro ? r : { entrada: { valores: r.valores } }; },
      gerar: gerarBST },
    avl: { titulo: "Árvore AVL", icone: "network", tom: "#8fbf6a",
      resumo: "A árvore que se endireita sozinha: veja fatores de balanceamento e rotações.",
      fato: "Altura ≤ ~1,44·log₂ n · busca, inserção e remoção em O(log n).",
      campos: [{ id: "v", rot: "Valores (2 a 12, sem repetir)", padrao: "10, 20, 30, 40, 50, 25" }],
      aleatorio: () => ({ v: sorteio(8, 1, 99).join(", ") }),
      ler: (f) => { const r = numeros(f.v, { qtd: 12, unicos: true }); return r.erro ? r : { entrada: { valores: r.valores } }; },
      gerar: gerarAVL },
  };

  const API = { SIMS, ROT, utils: { clonar, alt, fb, numeros } };
  raiz.AQSims = API;
  if (typeof module !== "undefined" && module.exports) module.exports = API;
})(typeof window !== "undefined" ? window : globalThis);
