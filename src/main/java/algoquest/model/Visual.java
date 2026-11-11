package algoquest.model;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class Visual {

    public static final int MAX_BYTES_IMAGEM = 300 * 1024;
    public static final int MAX_CARACTERES_SVG = 30_000;
    private static final String NS_SVG = "http://www.w3.org/2000/svg";
    private static final Set<String> ELEMENTOS = Set.of("svg", "g", "path", "circle", "ellipse", "rect", "line", "polyline",
            "polygon", "text", "tspan", "defs", "lineargradient", "radialgradient", "stop", "title", "desc", "marker", "clippath");

    private Visual() {
    }

    public static String validar(String bruto) {
        if (bruto == null || bruto.isBlank()) return null;
        String v = bruto.trim();
        if (v.startsWith("data:")) {
            return Avatar.validarImagem(v, MAX_BYTES_IMAGEM);
        }
        return validarSvg(v);
    }

    private static String validarSvg(String svg) {
        if (svg.length() > MAX_CARACTERES_SVG) throw new IllegalArgumentException("SVG grande demais (máximo 30.000 caracteres).");
        if (!svg.regionMatches(true, 0, "<svg", 0, 4)) throw new IllegalArgumentException("O SVG deve começar com <svg ...>.");
        Element raiz;
        try {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            f.setXIncludeAware(false);
            f.setExpandEntityReferences(false);
            raiz = f.newDocumentBuilder().parse(new ByteArrayInputStream(svg.getBytes(StandardCharsets.UTF_8))).getDocumentElement();
        } catch (Exception e) {
            throw new IllegalArgumentException("SVG inválido (XML malformado ou com DOCTYPE).");
        }
        if (!"svg".equals(raiz.getTagName())) throw new IllegalArgumentException("O elemento raiz deve ser <svg>.");
        conferir(raiz);
        if (!raiz.hasAttribute("xmlns")) {
            return "<svg xmlns=\"" + NS_SVG + "\"" + svg.substring(4);
        }
        if (!NS_SVG.equals(raiz.getAttribute("xmlns"))) throw new IllegalArgumentException("xmlns do SVG inválido.");
        return svg;
    }

    private static void conferir(Element e) {
        String nome = e.getTagName();
        if (!ELEMENTOS.contains(nome.toLowerCase())) {
            throw new IllegalArgumentException("Elemento SVG não permitido: <" + nome + ">.");
        }
        NamedNodeMap attrs = e.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            String a = attrs.item(i).getNodeName(), val = attrs.item(i).getNodeValue();
            String la = a.toLowerCase(), lv = val.toLowerCase().replaceAll("\\s+", "");
            if (la.startsWith("on")) throw new IllegalArgumentException("Atributo de evento não permitido: " + a + ".");
            if (la.equals("xmlns") || la.startsWith("xmlns:")) continue;
            if (la.equals("href") || la.endsWith(":href")) {
                if (!val.startsWith("#")) throw new IllegalArgumentException("Referências externas não são permitidas no SVG.");
            }
            if (lv.contains("javascript:") || lv.contains("data:") || lv.contains("http:") || lv.contains("https:") || lv.contains("//")) {
                throw new IllegalArgumentException("Valor de atributo não permitido no SVG (" + a + ").");
            }
        }
        for (Node n = e.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n instanceof Element filho) conferir(filho);
            else if (n.getNodeType() == Node.CDATA_SECTION_NODE || n.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
                throw new IllegalArgumentException("Conteúdo não permitido no SVG.");
            }
        }
    }
}
