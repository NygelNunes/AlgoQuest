import sys, json, pathlib
from PIL import Image

RAIZ = pathlib.Path(__file__).resolve().parent.parent
CATEGORIAS = [("herois", "Heróis", "Heroes"), ("aldeoes", "Aldeões", "Townsfolk"), ("monstros", "Monstros", "Monsters")]

CATALOGO = [
    ("hero_01_knight", "herois", "Cavaleiro", "M"), ("hero_02_paladin", "herois", "Paladino", "M"),
    ("hero_03_barbarian", "herois", "Bárbaro", "M"), ("hero_04_ranger", "herois", "Patrulheiro", "N"),
    ("hero_05_archer", "herois", "Arqueiro", "N"), ("hero_06_fire_mage", "herois", "Mago do Fogo", "M"),
    ("hero_07_ice_wizard", "herois", "Mago do Gelo", "M"), ("hero_08_cleric", "herois", "Clérigo", "N"),
    ("hero_09_rogue", "herois", "Ladino", "N"), ("hero_10_assassin", "herois", "Assassino", "N"),
    ("hero_11_bard", "herois", "Bardo", "M"), ("hero_12_druid", "herois", "Druida", "M"),
    ("hero_13_monk", "herois", "Monge", "M"), ("hero_14_battle_priest", "herois", "Sacerdote de Batalha", "M"),
    ("hero_15_spellblade", "herois", "Lâmina Arcana", "M"), ("hero_16_engineer", "herois", "Engenheiro", "M"),
    ("folk_01_blacksmith", "aldeoes", "Ferreiro", "M"), ("folk_02_merchant", "aldeoes", "Mercador", "M"),
    ("folk_03_innkeeper", "aldeoes", "Estalajadeira", "F"), ("folk_04_farmer", "aldeoes", "Fazendeiro", "M"),
    ("folk_05_town_guard", "aldeoes", "Guarda da Cidade", "M"), ("folk_06_priest", "aldeoes", "Sacerdote", "M"),
    ("folk_07_village_child", "aldeoes", "Criança da Vila", "F"), ("folk_08_village_elder", "aldeoes", "Ancião da Vila", "M"),
    ("folk_09_fisherman", "aldeoes", "Pescador", "M"), ("folk_10_miner", "aldeoes", "Mineiro", "M"),
    ("folk_11_baker", "aldeoes", "Padeira", "F"), ("folk_12_scholar", "aldeoes", "Erudito", "M"),
    ("folk_13_hunter", "aldeoes", "Caçador", "M"), ("folk_14_sailor", "aldeoes", "Marinheiro", "M"),
    ("folk_15_noble_lady", "aldeoes", "Dama Nobre", "F"), ("folk_16_jester", "aldeoes", "Bobo da Corte", "N"),
    ("monster_01_goblin", "monstros", "Goblin", "N"), ("monster_02_orc", "monstros", "Orc", "N"),
    ("monster_03_skeleton_warrior", "monstros", "Esqueleto Guerreiro", "N"), ("monster_04_zombie", "monstros", "Zumbi", "N"),
    ("monster_05_slime", "monstros", "Gosma", "N"), ("monster_06_demon", "monstros", "Demônio", "N"),
    ("monster_07_dragon", "monstros", "Dragão", "N"), ("monster_08_swamp_witch", "monstros", "Bruxa do Pântano", "N"),
    ("monster_09_troll", "monstros", "Troll", "N"), ("monster_10_vampire", "monstros", "Vampiro", "N"),
    ("monster_11_stone_golem", "monstros", "Golem de Pedra", "N"), ("monster_12_giant_spider", "monstros", "Aranha Gigante", "N"),
    ("monster_13_cave_bat", "monstros", "Morcego das Cavernas", "N"), ("monster_14_ghost", "monstros", "Fantasma", "N"),
    ("monster_15_cyclops", "monstros", "Ciclope", "N"), ("monster_16_dark_knight", "monstros", "Cavaleiro Sombrio", "N"),
]
TAMANHOS = {"p": 160, "g": 320}


def converter(origem: pathlib.Path):
    pastas = {c: p for c, _, p in CATEGORIAS}
    for d in TAMANHOS:
        (RAIZ / "frontend/assets/avatares" / d).mkdir(parents=True, exist_ok=True)
    total = 0
    for id_, cat, _, _ in CATALOGO:
        png = origem / pastas[cat] / (id_ + ".png")
        im = Image.open(png).convert("RGBA")
        for d, lado in TAMANHOS.items():
            saida = RAIZ / "frontend/assets/avatares" / d / (id_ + ".webp")
            im.resize((lado, lado), Image.LANCZOS).save(saida, "WEBP", quality=82, alpha_quality=100, method=6)
            total += saida.stat().st_size
    print(f"{len(CATALOGO)} retratos convertidos, {total/1024:.0f} KB no total")


def gerar_js():
    cats = [{"id": c, "nome": n} for c, n, _ in CATEGORIAS]
    itens = [{"id": i, "cat": c, "nome": n} for i, c, n, _ in CATALOGO]
    texto = ("const AVATAR_CATEGORIAS = " + json.dumps(cats, ensure_ascii=False) + ";\n"
             "const AVATAR_CATALOGO = [\n" + ",\n".join("  " + json.dumps(x, ensure_ascii=False) for x in itens) + "\n];\n")
    (RAIZ / "frontend/js/avatares-catalogo.js").write_text(texto, encoding="utf-8")


def gerar_java():
    def vetor(genero):
        return ", ".join('"%s"' % i for i, _, _, g in CATALOGO if g == genero)
    todos = ", ".join('"%s"' % i for i, _, _, _ in CATALOGO)
    texto = f'''package algoquest.model;

final class AvatarIds {{

    private AvatarIds() {{
    }}

    static final String[] TODOS = {{{todos}}};
    static final String[] FEMININOS = {{{vetor("F")}}};
    static final String[] MASCULINOS = {{{vetor("M")}}};
}}
'''
    (RAIZ / "src/main/java/algoquest/model/AvatarIds.java").write_text(texto, encoding="utf-8")


if __name__ == "__main__":
    if len(sys.argv) != 2:
        sys.exit("Uso: python3 tools/gerar_avatares.py CAMINHO/portraits-pack  (requer pillow; pasta com Heroes/, Monsters/, Townsfolk/)")
    converter(pathlib.Path(sys.argv[1]))
    gerar_js()
    gerar_java()
    print("catalogo JS e AvatarIds.java gerados")
