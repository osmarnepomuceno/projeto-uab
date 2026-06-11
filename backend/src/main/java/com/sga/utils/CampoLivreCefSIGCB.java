package com.sga.utils;
import org.jrimum.bopepo.campolivre.CampoLivre;
import org.jrimum.utilix.text.Field;
import org.jrimum.utilix.text.Filler;

public class CampoLivreCefSIGCB implements CampoLivre {

    /**
     *
     */
    private static final long serialVersionUID = 6194405988187683102L;

    String campoLivreFormatado = null;

    public CampoLivreCefSIGCB(String numeroConta, String dvNumConta, String nossoNumero) {

        String campo1 = formataCampo1(new Field<String>(numeroConta.substring(0, 5), 5, Filler.ZERO_LEFT).write());

        String campo2 = formataCampo2(numeroConta, dvNumConta, nossoNumero);

        String campo3 = formataCampo3(nossoNumero, campo1, campo2);

        // String dvCampo1 = UtilitarioString.calculaModulo10("1049"+campo1);
        // String dvCampo2 = UtilitarioString.calculaModulo10(campo2);
        // String dvCampo3 = UtilitarioString.calculaModulo10(campo3);

        // campo1 = campo1+dvCampo1;
        // campo2 = campo2+dvCampo2;
        // campo3 = campo3+dvCampo3;
        campoLivreFormatado = campo1 + campo2 + campo3;

    }

    @Override
    public void read(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public String write() {
        // TODO Auto-generated method stub
        return campoLivreFormatado;
    }

    private static String formataCampo1(String numero) {
        return numero;
    }

    public static String formataCampo2(String numeroCedente, String dvNumCedente, String nossoNumero) {

        numeroCedente = numeroCedente.substring(5, 6);
        String modalidade = nossoNumero.substring(0, 1);
        String identificador = nossoNumero.substring(1, 2);
        String nossoNumero1 = nossoNumero.substring(2, 5);
        String nossoNumero2 = nossoNumero.substring(5, 8);
        String numeroFormatado = numeroCedente + dvNumCedente + nossoNumero1 + modalidade + nossoNumero2 + identificador;

        return numeroFormatado;
    }

    public static String formataCampo3(String nossoNumero, String campo1, String campo2) {
        nossoNumero = nossoNumero.substring(8, 17);
        Integer dv = UtilitarioString.calculaDVModulo11(campo1 + campo2 + nossoNumero);

        return nossoNumero + dv;
    }
}
