package com.sga.service;

import com.sga.model.BoletoModel;
import com.sga.model.AssociadoModel;
import com.sga.model.EmpresaModel;
import com.sga.utils.CampoLivreCefSIGCB;
import com.sga.utils.UtilitarioString;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jrimum.bopepo.BancosSuportados;
import org.jrimum.bopepo.Boleto;
import org.jrimum.bopepo.view.BoletoViewer;
import org.jrimum.domkee.financeiro.banco.ParametrosBancariosMap;
import org.jrimum.domkee.financeiro.banco.febraban.*;
import org.jrimum.utilix.text.Field;
import org.jrimum.utilix.text.Filler;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.File;

import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@ApplicationScoped
public class BoletoService {

   private static final String CONTA = "047829";
   private static final String AGENCIA = "2525";
   private static final String ASSOCIACAO = "Associação Teste";
   private static final String CNPJ = "07.969.101/0001-14";
   private static final String DV_CONTA = "6";
   private static final String ENDERECO = "Rua Fulano de Tal, Numero 25, Palmas-TO CEP:77.000-000";

    @Inject
    BoletoStorageService boletoStorageService;


    public List<BoletoModel> listarPorAssociado(Integer associadoId) {
        return BoletoModel.find("associado.id", associadoId).list();
    }

    @Transactional
    public BoletoModel salvar(BoletoModel boleto) {
        boleto.persist();
        return boleto;
    }

    public byte[] gerarPdf(Integer boletoId) {
        BoletoModel boletoModel = BoletoModel.findById(boletoId);
        if (boletoModel == null) {
            throw new RuntimeException("Boleto não encontrado");
        }
        AssociadoModel associadoModel = boletoModel.associado;
        if (associadoModel == null) {
            throw new RuntimeException("Associado do boleto nao encontrado");
        }
        Boleto boleto = null;

        // Cedente ******************************************************
        Cedente cedente = new Cedente(ASSOCIACAO, CNPJ);
        // Sacado ******************************************************
        Sacado sacado = new Sacado(associadoModel.nome, associadoModel.cpf);

        BancosSuportados bancosSuportados = BancosSuportados.CAIXA_ECONOMICA_FEDERAL;
        if (bancosSuportados == null) {
            throw new RuntimeException("Banco nao Suportado");
        }

        ContaBancaria contaBancaria = new ContaBancaria(bancosSuportados.create());
        contaBancaria.setNumeroDaConta(new NumeroDaConta(Integer.parseInt(CONTA)));
        contaBancaria.setCarteira(new Carteira( 1, TipoDeCobranca.SEM_REGISTRO, "SR"));

        contaBancaria.setAgencia(new Agencia(Integer.parseInt(AGENCIA)));

        Titulo titulo = new Titulo(contaBancaria, sacado, cedente);
        titulo.setNumeroDoDocumento(geraNumeroDocumento(boletoId));

        ParametrosBancariosMap parametrosBancariosMap = new ParametrosBancariosMap();

        titulo.setDataDoDocumento(new Date());
        titulo.setDataDoVencimento(Date.from(
                boletoModel.dataVencimento.minusYears(2).atStartOfDay(ZoneId.systemDefault()).toInstant()));

        titulo.setValor(boletoModel.valor);
        titulo.setTipoDeDocumento(TipoDeTitulo.OUTROS);

        titulo.setNossoNumero(geraNossoNumeroSIGCBCaixa(boletoModel.id));
        titulo.setDigitoDoNossoNumero(UtilitarioString.calculaDVModulo11(titulo.getNossoNumero()) + "");
        CampoLivreCefSIGCB campoLivreCefSIGCB = new CampoLivreCefSIGCB(CONTA,DV_CONTA, titulo.getNossoNumero() + "");
        titulo.setParametrosBancarios(parametrosBancariosMap);
        boleto = new Boleto(titulo, campoLivreCefSIGCB);

        boleto.addTextosExtras("txtFcAgenciaCodigoCedente", AGENCIA + "/" + CONTA + "-" + DV_CONTA);
        boleto.addTextosExtras("txtRsAgenciaCodigoCedente", AGENCIA + "/" + CONTA + "-" + DV_CONTA);
        boleto.addTextosExtras("txtFcAceite", "N");
        boleto.setLocalPagamento("Pagável preferencialmente em qualquer banco até a data de vencimento.");
        boleto.addTextosExtras("txtRsInstituicao", ASSOCIACAO);
        boleto.addTextosExtras("txtFcCedenteEndereco", ENDERECO);
        boleto.addTextosExtras("txtRcCedenteEndereco",ENDERECO);
        boleto.addTextosExtras("txtRsEndereco", ENDERECO);
        boleto.addTextosExtras("txtRsSacado", associadoModel.nome);

        boleto.addTextosExtras("txtRsEndereco2", "CNPJ: " + CNPJ + " - FONE: 3232-3232 ");

        boleto.addTextosExtras("txtFcCpfCnpj", CNPJ);

        boleto.addTextosExtras("txtFcEspecie","R$");

        boleto.addTextosExtras("txtFcSacadoL1", associadoModel.nome);
        boleto.addTextosExtras("txtFcSacadoCpfCnpj", associadoModel.cpf);
        boleto.addTextosExtras("txtRsSacadoCpfCnpj", associadoModel.cpf);

        boleto.addTextosExtras("txtFcSacadoL2", ENDERECO);

        File template =getTemplateFile();

        BoletoViewer boletoViewer = new BoletoViewer(boleto);
        boletoViewer.setTemplate(template);

        Path boletoPath = boletoStorageService.resolvePdf("boleto-" + boletoId);


        boletoViewer.getPdfAsFile(boletoPath.toString());







        try {
            return Files.readAllBytes(boletoPath);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao ler boleto PDF gerado em: " + boletoPath, e);
        }
    }

    public static String geraNumeroDocumento(Integer idTipoGuia) {

        String numeroDoc;

        @SuppressWarnings("deprecation")
        Date dataBase = new Date("01/01/2012");
        Date dataAtual = Calendar.getInstance().getTime();
        long timeDiff = dataAtual.getTime() - dataBase.getTime();
        int dias = 0;
        double temp;

        temp = timeDiff / 1000; // Convertendo Segundos;
        temp /= 60; // Convertendo Minutos;
        temp /= 60; // Convertendo Hora;
        dias = (int) (temp / 24); // Convertendo Dia;

        numeroDoc = new Field<String>(idTipoGuia + "", 5, Filler.ZERO_LEFT).write() + new Field<String>(dias + "", 4, Filler.ZERO_LEFT).write();

        return numeroDoc;

    }

    public static String geraNossoNumeroSIGCBCaixa(Integer idGuia) {

        String nossoNumero = null;
        nossoNumero = "24" + new Field<String>(idGuia + "", 15, Filler.ZERO_LEFT).write();
        return nossoNumero;

    }

    public File getTemplateFile() {
        try (InputStream input = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream("GuiaAssistencialTemplateCaixa.pdf")) {

            if (input == null) {
                throw new IllegalStateException("Template PDF nao encontrado.");
            }

            Path tempFile = boletoStorageService.createTempPdf("GuiaAssistencialTemplateCaixa-");
            Files.copy(input, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            return tempFile.toFile();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao carregar template PDF.", e);
        }

    }
}
