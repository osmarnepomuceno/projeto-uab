package com.sga.utils;

import java.text.ParseException;
import java.util.InputMismatchException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jrimum.utilix.text.Field;
import org.jrimum.utilix.text.Filler;
import org.jrimum.vallia.digitoverificador.Modulo;
import org.jrimum.vallia.digitoverificador.TipoDeModulo;

public class UtilitarioString {

    public static String formatarCNPJ(String cnpj) throws ParseException {
        if (cnpj.length() < 14) {
            return null;
        } else {
            cnpj = cnpj.substring(0, 2) + "." + cnpj.substring(2, 5) + "." + cnpj.substring(5, 8) + "/" + cnpj.substring(8, 12) + "-" + cnpj.substring(12, 14);
        }

        return cnpj;
    }

    public static String formatarCEI(String cei) throws ParseException {
        if (cei.length() < 12) {
            return null;
        } else {
            cei = cei.substring(0, 2) + "." + cei.substring(2, 5) + "." + cei.substring(5, 10) + "/" + cei.substring(10, 12);
        }

        return cei;
    }

    public static String formatarCPF(String cpf) throws ParseException {
        if (cpf.length() < 11) {
            return null;
        } else {
            cpf = cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "." + cpf.substring(6, 9) + "-" + cpf.substring(9, 11);
        }

        return cpf;
    }

    public static String formatarCEP(String cep) throws ParseException {
        if (cep.length() < 8) {
            return null;
        } else {
            cep = cep.substring(0, 2) + "." + cep.substring(2, 5) + "-" + cep.substring(5, 8);
        }

        return cep;
    }

    public static String retiraMascaraEDVCNPJ(String cnpj) {
        cnpj = cnpj.replace(".", "");
        cnpj = cnpj.replace("-", "");
        cnpj = cnpj.replace("/", "");
        cnpj = cnpj.substring(0, 12);
        return cnpj;
    }

    public static String calculaModulo10(String numero) {

        Modulo modulo10 = new Modulo(TipoDeModulo.MODULO10);

        int restoDivisao = modulo10.calcule(numero);

        int restoSubtracao = (10 - restoDivisao);
        String dV;
        if (restoSubtracao == 10)
            dV = "0";
        else
            dV = "" + restoSubtracao;

        return dV;
    }

    public static int calculaDVModulo11(String numero) {
        int dv = 0;
        int[] values = new int[numero.length()];
        for (int i = 0; i < numero.length(); i++) {
            values[i] = Integer.parseInt(numero.charAt(i) + "");
        }
        int soma = 0;
        int vetpos = numero.length() - 1;
        while (vetpos >= 0) {
            for (int i = 2; i < 10; i++) {
                soma += values[vetpos] * i;
                vetpos--;
                if (vetpos < 0)
                    break;
            }
        }

        if (soma < 11) {
            dv = soma - 11;
        } else {
            int resto = soma % 11;
            dv = 11 - resto;
        }

        if (dv > 9)
            dv = 0;

        return dv;
    }

    public static int calculaDVModulo11Banestes(String numero) {
        int dv = 0;
        // int divisao = 0;
        int[] values = new int[numero.length()];
        for (int i = 0; i < numero.length(); i++) {
            values[i] = Integer.parseInt(numero.charAt(i) + "");
        }
        int soma = 0;
        int vetpos = numero.length() - 1;
        while (vetpos >= 0) {
            for (int i = 2; i < 10; i++) {
                soma += values[vetpos] * i;
                vetpos--;
                if (vetpos < 0)
                    break;
            }
        }

        if (soma < 11) {
            dv = 11 - soma;
        } else {
            int resto = soma % 11;
            dv = 11 - resto;
        }

        if (dv > 9)
            dv = 0;

        return dv;
    }

    public static String geraNossoNumeroSIGCBCaixa(Integer idGuia) {

        String nossoNumero = null;
        nossoNumero = "24" + new Field<String>(idGuia + "", 15, Filler.ZERO_LEFT).write();
        return nossoNumero;

    }

    public static boolean isCNPJ(String CNPJ) {
        // considera-se erro CNPJ's formados por uma sequencia de numeros iguais
        if (CNPJ.equals("00000000000000") || CNPJ.equals("11111111111111") || CNPJ.equals("22222222222222") || CNPJ.equals("33333333333333") || CNPJ.equals("44444444444444")
                || CNPJ.equals("55555555555555") || CNPJ.equals("66666666666666") || CNPJ.equals("77777777777777") || CNPJ.equals("88888888888888")
                || CNPJ.equals("99999999999999") || (CNPJ.length() != 14))
            return (false);

        char dig13, dig14;
        int sm, i, r, num, peso;

        // "try" - protege o codigo para eventuais erros de conversao de tipo
        // (int)
        try {
            // Calculo do 1o. Digito Verificador
            sm = 0;
            peso = 2;
            for (i = 11; i >= 0; i--) {
                // converte o i-esimo caractere do CNPJ em um numero:
                // por exemplo, transforma o caractere '0' no inteiro 0
                // (48 eh a posicao de '0' na tabela ASCII)
                num = (int) (CNPJ.charAt(i) - 48);
                sm = sm + (num * peso);
                peso = peso + 1;
                if (peso == 10)
                    peso = 2;
            }

            r = sm % 11;
            if ((r == 0) || (r == 1))
                dig13 = '0';
            else
                dig13 = (char) ((11 - r) + 48);

            // Calculo do 2o. Digito Verificador
            sm = 0;
            peso = 2;
            for (i = 12; i >= 0; i--) {
                num = (int) (CNPJ.charAt(i) - 48);
                sm = sm + (num * peso);
                peso = peso + 1;
                if (peso == 10)
                    peso = 2;
            }

            r = sm % 11;
            if ((r == 0) || (r == 1))
                dig14 = '0';
            else
                dig14 = (char) ((11 - r) + 48);

            // Verifica se os digitos calculados conferem com os digitos
            // informados.
            // System.out.println(dig13+""+dig14);
            if ((dig13 == CNPJ.charAt(12)) && (dig14 == CNPJ.charAt(13)))
                return (true);
            else
                return (false);
        } catch (InputMismatchException erro) {
            return (false);
        }
    }

    /**
     * Metodo recebe o CNPJ sem o DV
     *
     * @param CNPJ
     * @return Retorna o DV do CNPJ
     * @author OsmarJunior
     * @since 02/04/2012
     */
    public static String calculaDvCNPJ(String CNPJ) {
        // considera-se erro CNPJ's formados por uma sequencia de numeros iguais
        if (CNPJ.equals("00000000000000") || CNPJ.equals("11111111111111") || CNPJ.equals("22222222222222") || CNPJ.equals("33333333333333") || CNPJ.equals("44444444444444")
                || CNPJ.equals("55555555555555") || CNPJ.equals("66666666666666") || CNPJ.equals("77777777777777") || CNPJ.equals("88888888888888")
                || CNPJ.equals("99999999999999"))
            return null;

        char dig13, dig14;
        int sm, i, r, num, peso;

        // "try" - protege o codigo para eventuais erros de conversao de tipo
        // (int)
        try {
            // Calculo do 1o. Digito Verificador
            sm = 0;
            peso = 2;
            for (i = 11; i >= 0; i--) {
                // converte o i-esimo caractere do CNPJ em um numero:
                // por exemplo, transforma o caractere '0' no inteiro 0
                // (48 eh a posicao de '0' na tabela ASCII)
                num = (int) (CNPJ.charAt(i) - 48);
                sm = sm + (num * peso);
                peso = peso + 1;
                if (peso == 10)
                    peso = 2;
            }

            r = sm % 11;
            if ((r == 0) || (r == 1))
                dig13 = '0';
            else
                dig13 = (char) ((11 - r) + 48);

            // Calculo do 2o. Digito Verificador
            CNPJ = CNPJ + "" + dig13;
            sm = 0;
            peso = 2;
            for (i = 12; i >= 0; i--) {
                num = (int) (CNPJ.charAt(i) - 48);
                sm = sm + (num * peso);
                peso = peso + 1;
                if (peso == 10)
                    peso = 2;
            }

            r = sm % 11;
            if ((r == 0) || (r == 1))
                dig14 = '0';
            else
                dig14 = (char) ((11 - r) + 48);

            // Verifica se os digitos calculados conferem com os digitos
            // informados.
            return dig13 + "" + dig14;

        } catch (InputMismatchException erro) {
            return null;
        }
    }

    public static boolean isCPF(String CPF) {
        // considera-se erro CPF's formados por uma sequencia de numeros iguais
        if (CPF.equals("00000000000") || CPF.equals("11111111111") || CPF.equals("22222222222") || CPF.equals("33333333333") || CPF.equals("44444444444")
                || CPF.equals("55555555555") || CPF.equals("66666666666") || CPF.equals("77777777777") || CPF.equals("88888888888") || CPF.equals("99999999999")
                || (CPF.length() != 11))
            return (false);

        char dig10, dig11;
        int sm, i, r, num, peso;

        // "try" - protege o codigo para eventuais erros de conversao de tipo
        // (int)
        try {
            // Calculo do 1o. Digito Verificador
            sm = 0;
            peso = 10;
            for (i = 0; i < 9; i++) {
                // converte o i-esimo caractere do CPF em um numero:
                // por exemplo, transforma o caractere '0' no inteiro 0
                // (48 eh a posicao de '0' na tabela ASCII)
                num = (int) (CPF.charAt(i) - 48);
                sm = sm + (num * peso);
                peso = peso - 1;
            }

            r = 11 - (sm % 11);
            if ((r == 10) || (r == 11))
                dig10 = '0';
            else
                dig10 = (char) (r + 48); // converte no respectivo caractere
            // numerico

            // Calculo do 2o. Digito Verificador
            sm = 0;
            peso = 11;
            for (i = 0; i < 10; i++) {
                num = (int) (CPF.charAt(i) - 48);
                sm = sm + (num * peso);
                peso = peso - 1;
            }

            r = 11 - (sm % 11);
            if ((r == 10) || (r == 11))
                dig11 = '0';
            else
                dig11 = (char) (r + 48);

            // Verifica se os digitos calculados conferem com os digitos
            // informados.
            if ((dig10 == CPF.charAt(9)) && (dig11 == CPF.charAt(10)))
                return (true);
            else
                return (false);
        } catch (InputMismatchException erro) {
            return (false);
        }
    }

    public static boolean isCEI(String cei) {
        if (cei != null && cei.length() < 12) {
            return false;
        }
        Integer num, soma = 0;
        String peso = "74185216374";
        for (int i = 0; i < 11; i++) {
            num = (int) (cei.charAt(i) - 48);
            int multipilicador = (peso.charAt(i) - 48);
            soma = soma + (num * multipilicador);

        }
        // System.out.println(soma);
        int d1 = ((10 - (soma % 10 + (soma / 10)) % 10) % 10);
        d1 = Math.abs(d1);
        if (d1 == cei.charAt(11) - 48) {
            return true;
        } else {
            return false;
        }
    }

    public static String pegarUltimosCaracteresDaString(String str, Integer qntd) {

        if (str != null && !"".equals(str) && qntd != null && qntd != 0) {
            str = str.substring(str.length() - qntd);
        } else {
            str = "";
        }

        return str;
    }

    public static String removeMascarasCeiCnpjCpf(String str) {

        str = str.replaceAll("[^0-9]", "");

        return str;
    }

    public static boolean isInteiro(String s) {

        // cria um array de char
        // char[] c = s.toCharArray();
        boolean d = true;

        for (int i = 0; i < s.length(); i++) {
            // verifica se o char nao e um digito
            if (!Character.isDigit(s.charAt(i))) {
                d = false;
                break;
            }
        }

        return d;

    }

    public static String codigoSindicalParaExibir(String codigoSindical, Integer tipoEntidade) {
        String codigoCedente = "";
        if (tipoEntidade == 1 || tipoEntidade == 0) {
            codigoCedente = "S-" + codigoSindical.substring(8, 13);
        } else if (tipoEntidade == 2) {
            codigoCedente = "F-" + codigoSindical.substring(4, 7);
        } else if (tipoEntidade == 3) {
            codigoCedente = "C-" + codigoSindical.substring(0, 3);
        }

        return codigoCedente;
    }

    public static String truncaAposBarra(String caracteres) {

        try {

            if (caracteres != null || !"".equals("")) {

                String patternStr = "[^0-9]*";
                String replaceStr = "";
                Pattern pattern = Pattern.compile(patternStr);
                Matcher matcher = pattern.matcher(caracteres);
                caracteres = matcher.replaceAll(replaceStr);
                System.out.println(caracteres);
                /*
                 * caracteres = caracteres.replaceAll("-", ""); caracteres =
                 * caracteres.replaceAll("A-Z", ""); caracteres =
                 * caracteres.substring(0,caracteres.indexOf("/"));
                 */
            } else {
                caracteres = "0";
                System.out.println("Caracteres null ");
            }

        } catch (Exception e) {
            System.out.println("erro ao truncar" + caracteres);
            System.out.println(e.getMessage());
        }
        return caracteres;
    }

    public static String matriculaDependente(String idAssociado, String idDependente) {
        String matricula = new Field<String>(idAssociado, 6, Filler.ZERO_LEFT).write();
        matricula = matricula + "-" + new Field<String>(idDependente, 6, Filler.ZERO_LEFT).write();

        return matricula;

    }

    public static String retiraMascaras(String campo) {
        campo = campo.trim();
        char[] v = campo.toCharArray();
        StringBuffer res = new StringBuffer("");
        for (int i = 0; i < v.length; i++) {
            if (Character.isLetterOrDigit(v[i])) {
                res.append(v[i]);
            }
        }
        return res.toString();
    }

    public static String formataFone(String fone) throws ParseException {
        if (fone.length() < 10) {
            return null;
        } else {
            fone = "(" + fone.substring(0, 2) + ")" + " " + fone.substring(2, 6) + "-" + fone.substring(6, 10);
        }

        return fone;
    }

    public static String retiraZeroEsquerda(String x) {
        String temp = "";
        x = x.replaceAll("[^\\p{ASCII}]", "");
        for (int i = 0; i < x.length(); i++) {
            if (!x.substring(i, i + 1).equals("0")) { // checa se chegou no
                // primeiro caracter q
                // não é 0
                temp += x.substring(i, x.length()); // temp fica com os valores
                // correspondentes à
                // substring da posição
                // atual ate o fim
                break; // sai do laço
            }
        }
        // System.out.println("Antes: " + x + " Depois:" + temp);
        return temp;
    }

    public static String inserirCaracterNaString(String str, String caracter, Integer posicao) {

        StringBuilder stringBuilder = new StringBuilder(str);
        stringBuilder.insert(posicao, caracter);

        return stringBuilder.toString();
    }

    public static String removeAcentos(String s) {
        if (s == null) {
            return "";
        }
        String semAcentos = s.toLowerCase();
        semAcentos = semAcentos.replaceAll("[áàâãä]", "a");
        semAcentos = semAcentos.replaceAll("[éèêë]", "e");
        semAcentos = semAcentos.replaceAll("[íìîï]", "i");
        semAcentos = semAcentos.replaceAll("[óòôõö]", "o");
        semAcentos = semAcentos.replaceAll("[úùûü]", "u");
        semAcentos = semAcentos.replaceAll("ç", "c");
        semAcentos = semAcentos.replaceAll("ñ", "n");
        return semAcentos;
    }

    public static String removeCaracteresEspeciais(String str) {
        String[] caracteresEspeciais = {"\\.", ",", "-", ":", "\\(", "\\)", "ª", "\\|", "\\\\", "°", ""};

        if (str == null) {
            return null;
        }

        for (int i = 0; i < caracteresEspeciais.length; i++) {
            str = str.replaceAll(caracteresEspeciais[i], "");
        }
        return str;
    }

    public static String getName(String base) {
        return base + System.currentTimeMillis();
    }

// public static String montaEnderecoCompleto(Object entidade) {
// String endCompleto = null;
// if (entidade != null) {
// if (entidade.getClass(). .getEndereco() != null) {
// endCompleto = getEndereco();
// }
// if (getBairro() != null) {
// endCompleto = " Bairro: " + getBairro();
// }
// if (endCompleto != null) {
// endCompleto = " - " + getCidade().getDescCidade() + " - " +
// getCidade().getUf().getSiglaUF();
// } else {
// endCompleto = getCidade().getDescCidade() + " - " +
// getCidade().getUf().getSiglaUF();
// }
//
// if (getCep() != null) {
// endCompleto = " CEP: " + getCep();
// }
// }
//
// }

}