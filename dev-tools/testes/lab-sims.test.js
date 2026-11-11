const assert = require("assert");
const { SIMS, utils } = require("../../frontend/js/lab-sims.js");
let ok = 0; const t = (n, f) => { f(); ok++; console.log("ok -", n); };
const ler = (id, f) => { const r = SIMS[id].ler(f); assert(!r.erro, r.erro); return r.entrada; };
const emOrdem = (t) => (t ? [...emOrdem(t.l), t.v, ...emOrdem(t.r)] : []);
const bst = (t, lo = -1e9, hi = 1e9) => !t || (t.v > lo && t.v < hi && bst(t.l, lo, t.v) && bst(t.r, t.v, hi));
const avl = (t) => !t || (Math.abs(utils.fb(t)) <= 1 && avl(t.l) && avl(t.r));
const perguntasValidas = (ps) => ps.forEach((p) => { if (p.pergunta) { assert(p.pergunta.certa >= 0 && p.pergunta.certa < p.pergunta.opcoes.length); assert(p.pergunta.explica); } });

t("bubble ordena e marca fim", () => {
  for (let k = 0; k < 50; k++) {
    const e = ler("bubble", SIMS.bubble.aleatorio()); const ps = SIMS.bubble.gerar(e);
    const fim = ps[ps.length - 1]; assert(fim.fim);
    assert.deepStrictEqual(fim.a.map((x) => x.v), [...e.valores].sort((a, b) => a - b)); perguntasValidas(ps);
  }
  const ps = SIMS.bubble.gerar(ler("bubble", { v: "1, 2, 3, 4" }));
  assert(ps.filter((p) => p.pergunta).length === 3);
});
t("busca binaria acha e nao acha", () => {
  const a = SIMS.busca.gerar(ler("busca", { v: "3, 8, 12, 17, 21, 26, 33, 40, 47, 55, 62, 71", alvo: "47" }));
  assert(a.some((p) => p.achou === 8)); perguntasValidas(a);
  const b = SIMS.busca.gerar(ler("busca", { v: "2, 4, 6", alvo: "5" })); assert(b[b.length - 1].fim && /não está/.test(b[b.length - 1].txt));
  for (let k = 0; k < 50; k++) { const f = SIMS.busca.aleatorio(); const ps = SIMS.busca.gerar(ler("busca", f)); assert(ps.some((p) => p.achou != null)); }
});
t("hash posicao = k mod m e colisoes", () => {
  const e = ler("hash", { v: "15, 22, 8, 36", m: "7" }); const ps = SIMS.hash.gerar(e); perguntasValidas(ps);
  const ult = ps[ps.length - 1]; assert.deepStrictEqual(ult.tab[1], [15, 22, 8, 36]);
  assert(ps.filter((p) => p.colisao).length === 3);
  const q = ps.find((p) => p.pergunta); assert.strictEqual(q.pergunta.opcoes[q.pergunta.certa], "Posição 1");
});
t("bst mantem invariante e em-ordem", () => {
  for (let k = 0; k < 50; k++) {
    const e = ler("bst", SIMS.bst.aleatorio()); const ps = SIMS.bst.gerar(e); perguntasValidas(ps);
    const fim = ps[ps.length - 1]; assert(bst(fim.arvore)); assert.deepStrictEqual(emOrdem(fim.arvore), [...e.valores].sort((a, b) => a - b));
  }
});
t("avl sempre balanceada, em cada retrato apos rotacao", () => {
  for (let k = 0; k < 200; k++) {
    const e = ler("avl", SIMS.avl.aleatorio()); const ps = SIMS.avl.gerar(e); perguntasValidas(ps);
    const fim = ps[ps.length - 1]; assert(bst(fim.arvore) && avl(fim.arvore)); assert.deepStrictEqual(emOrdem(fim.arvore), [...e.valores].sort((a, b) => a - b));
    ps.forEach((p, i) => { if (/rebalanceada/.test(p.txt)) assert(avl(p.arvore), "passo " + i); });
  }
  const ps = SIMS.avl.gerar(ler("avl", { v: "10, 20, 30, 40, 50, 25" }));
  const rots = ps.filter((p) => p.pergunta && p.pergunta.opcoes.length === 4).map((p) => p.pergunta.opcoes[p.pergunta.certa]);
  assert.deepStrictEqual(rots, ["Rotação simples à esquerda", "Rotação simples à esquerda", "Rotação dupla direita-esquerda"]);
  const mono = SIMS.avl.gerar(ler("avl", { v: "3, 2, 1" })).filter((p) => p.pergunta && p.pergunta.opcoes.length === 4);
  assert.strictEqual(mono[0].pergunta.opcoes[mono[0].pergunta.certa], "Rotação simples à direita");
  const lr = SIMS.avl.gerar(ler("avl", { v: "30, 10, 20" })).filter((p) => p.pergunta && p.pergunta.opcoes.length === 4);
  assert.strictEqual(lr[0].pergunta.opcoes[lr[0].pergunta.certa], "Rotação dupla esquerda-direita");
});
t("validacao de entrada em portugues", () => {
  assert(SIMS.bubble.ler({ v: "1" }).erro); assert(SIMS.bubble.ler({ v: "a, b" }).erro); assert(SIMS.avl.ler({ v: "1, 1" }).erro);
  assert(SIMS.hash.ler({ v: "1, 2", m: "2" }).erro); assert(SIMS.busca.ler({ v: "1, 2", alvo: "x" }).erro);
});
console.log("\n" + ok + " grupos de teste passaram");
