const TEMAS = {
  arvore:   { tom: "#3f9a5a", icone: "network" },
  busca:    { tom: "#b89a5e", icone: "search" },
  ordenacao:{ tom: "#d0343e", icone: "arrow-down-up" },
  hash:     { tom: "#6f8fb5", icone: "hash" },
  analise:  { tom: "#b9bfca", icone: "activity" },
  padrao:   { tom: "#8c93a1", icone: "book-open" },
};

function temaDe(titulo) {
  const t = (titulo || "").toLowerCase();
  if (t.includes("busca")) return "busca";
  if (t.includes("ordena")) return "ordenacao";
  if (t.includes("hash")) return "hash";
  if (t.includes("estrutura") || t.includes("árvore") || t.includes("arvore")) return "arvore";
  if (t.includes("anális") || t.includes("analis") || t.includes("big")) return "analise";
  return "padrao";
}

const CORPOS = {
  arvore() {
    const nos = [[160,20],[100,50],[220,50],[70,82],[130,82],[190,82],[250,82]];
    const folhas = [55,85,115,145,175,205,235,265].map((x) => [x,116]);
    const lig = [[0,1],[0,2],[1,3],[1,4],[2,5],[2,6]];
    const lf = [[3,0],[3,1],[4,2],[4,3],[5,4],[5,5],[6,6],[6,7]];
    return `<path class="f" d="M24 128H296"/>` +
      lig.map(([a,b]) => `<path class="l" d="M${nos[a]}L${nos[b]}"/>`).join("") +
      lf.map(([a,b]) => `<path class="l" style="opacity:.55" d="M${nos[a]}L${folhas[b]}"/>`).join("") +
      `<path class="l fx" d="M160 20L100 50L130 82L145 116"/>` +
      nos.map(([x,y],i) => `<circle class="n" cx="${x}" cy="${y}" r="${i ? 8 : 11}"/><circle class="h" cx="${x}" cy="${y}" r="${i ? 2.3 : 3.6}"/>`).join("") +
      folhas.map(([x,y]) => `<circle class="n" cx="${x}" cy="${y}" r="5"/>`).join("") +
      `<circle class="f" cx="160" cy="20" r="26"/>`;
  },
  busca() {
    return [24,46,68,92].map((r) => `<circle class="f" cx="236" cy="70" r="${r}"/>`).join("") +
      `<path class="f" d="M236 6V134M172 70H300M191 25l90 90M281 25l-90 90"/>` +
      `<path class="l fx" d="M20 116C76 116 70 58 124 70S196 104 236 70"/>` +
      [[20,116],[124,70],[196,92]].map(([x,y]) => `<circle class="n" cx="${x}" cy="${y}" r="6"/>`).join("") +
      `<circle class="n" cx="236" cy="70" r="10"/><circle class="h" cx="236" cy="70" r="4.5"/>`;
  },
  ordenacao() {
    const alt = [26,44,52,70,82,96,110];
    return alt.map((h,i) => `<rect class="n" x="${34+i*36}" y="${128-h}" width="26" height="${h}"/><path class="l" d="M${34+i*36} ${128-h}h26"/><path class="f" d="M${47+i*36} ${134-h}V124"/><circle class="h" cx="${47+i*36}" cy="${134-h}" r="2.5"/>`).join("") +
      `<path class="l fx" d="M${47+2*36} ${128-52-12}C${47+2*36} ${128-52-34} ${47+3*36} ${128-70-34} ${47+3*36} ${128-70-12}"/>` +
      `<path class="f" d="M20 128H300M20 118H300"/>`;
  },
  hash() {
    let c = "";
    for (let r = 0; r < 3; r++) for (let k = 0; k < 6; k++) {
      c += `<rect class="n" x="${118+k*32}" y="${22+r*36}" width="28" height="30"/>`;
      if ((r * 6 + k) % 4 === 1) c += `<path class="h" d="M${132+k*32} ${30+r*36}l6 7-6 7-6-7z"/>`;
    }
    return c + [0,1,2].map((r) => `<circle class="n" cx="34" cy="${37+r*36}" r="8"/><path class="l fx" d="M44 ${37+r*36}H112"/><path class="f" d="M34 ${37+r*36}m-16 0h-6"/>`).join("") +
      `<path class="f" d="M118 18H304M118 130H304"/>`;
  },
  analise() {
    return `<path class="f" d="M40 20V120H290M40 100H290M40 80H290M40 60H290M40 40H290"/>` +
      `<path class="l" d="M40 120L290 36" style="opacity:.55"/><path class="l fx" d="M40 120C100 66 160 52 290 46"/><path class="l" d="M40 120C150 118 236 94 290 16"/>` +
      `<circle class="n" cx="290" cy="36" r="5"/><circle class="n" cx="290" cy="46" r="5"/><circle class="n" cx="290" cy="16" r="5"/><circle class="h" cx="40" cy="120" r="4"/>`;
  },
  padrao() {
    return [18,36,54].map((r) => `<circle class="${r === 36 ? "l" : "f"}" cx="160" cy="70" r="${r}"/>`).join("") +
      `<path class="f" d="M160 8V132M98 70H222"/><circle class="h" cx="160" cy="70" r="5"/>`;
  },
};

function arteSvg(tema) {
  return `<svg class="arte-svg" viewBox="0 0 320 140" preserveAspectRatio="xMidYMid slice" aria-hidden="true">${CORPOS[tema]()}</svg>`;
}

function temaDoModulo(m) {
  return m && m.tema && TEMAS[m.tema] ? m.tema : temaDe(m && m.titulo);
}

function visualSrc(v) {
  if (!v) return null;
  if (v.startsWith("data:image/")) return v;
  if (/^\s*<svg/i.test(v)) return "data:image/svg+xml;base64," + btoa(unescape(encodeURIComponent(v)));
  return null;
}
