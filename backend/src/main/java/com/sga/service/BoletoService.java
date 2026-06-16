package com.sga.service;

import com.sga.model.AssociadoModel;
import com.sga.model.BoletoModel;
import com.sga.utils.CampoLivreCefSIGCB;
import com.sga.utils.UtilitarioString;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jrimum.bopepo.BancosSuportados;
import org.jrimum.bopepo.Boleto;
import org.jrimum.bopepo.view.BoletoViewer;
import org.jrimum.domkee.financeiro.banco.ParametrosBancariosMap;
import org.jrimum.domkee.financeiro.banco.febraban.Agencia;
import org.jrimum.domkee.financeiro.banco.febraban.Carteira;
import org.jrimum.domkee.financeiro.banco.febraban.Cedente;
import org.jrimum.domkee.financeiro.banco.febraban.ContaBancaria;
import org.jrimum.domkee.financeiro.banco.febraban.NumeroDaConta;
import org.jrimum.domkee.financeiro.banco.febraban.Sacado;
import org.jrimum.domkee.financeiro.banco.febraban.TipoDeCobranca;
import org.jrimum.domkee.financeiro.banco.febraban.TipoDeTitulo;
import org.jrimum.domkee.financeiro.banco.febraban.Titulo;
import org.jrimum.utilix.text.Field;
import org.jrimum.utilix.text.Filler;

import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@ApplicationScoped
public class BoletoService {

    private static final String CONTA = "047829";
    private static final String AGENCIA = "2525";
    private static final String ASSOCIACAO = "Associacao Teste";
    private static final String CNPJ = "07.969.101/0001-14";
    private static final String DV_CONTA = "6";
    private static final String ENDERECO = "Rua Fulano de Tal, Numero 25, Palmas-TO CEP:77.000-000";

    @Inject
    BoletoStorageService boletoStorageService;

    @Inject
    BoletoPdfJobQueue boletoPdfJobQueue;

    public List<BoletoModel> listarPorAssociado(Integer associadoId) {
        return BoletoModel.find("associado.id", associadoId).list();
    }

    @Transactional
    public BoletoModel salvar(BoletoModel boleto) {
        boleto.persist();
        return boleto;
    }

    public byte[] gerarPdf(Integer boletoId) {
        return boletoStorageService.readCachedPdf(pdfFileName(boletoId))
                .orElseGet(() -> boletoPdfJobQueue.execute(boletoId, () -> gerarPdfSemCache(carregarDadosPdf(boletoId))));
    }

    private BoletoPdfData carregarDadosPdf(Integer boletoId) {
        BoletoModel boletoModel = BoletoModel.findById(boletoId);
        if (boletoModel == null) {
            throw new RuntimeException("Boleto nao encontrado");
        }

        AssociadoModel associadoModel = boletoModel.associado;
        if (associadoModel == null) {
            throw new RuntimeException("Associado do boleto nao encontrado");
        }

        return new BoletoPdfData(
                boletoModel.id,
                boletoModel.valor,
                boletoModel.dataVencimento,
                associadoModel.nome,
                associadoModel.cpf
        );
    }

    private byte[] gerarPdfSemCache(BoletoPdfData dadosPdf) {
        Boleto boleto = criarBoleto(dadosPdf);

        BoletoViewer boletoViewer = new BoletoViewer(boleto);
        boletoViewer.setTemplate(getTemplateFile());

        Path boletoPath = boletoStorageService.resolvePdf(pdfFileName(dadosPdf.boletoId()));
        boletoViewer.getPdfAsFile(boletoPath.toString());

        return boletoStorageService.readPdf(boletoPath);
    }

    private Boleto criarBoleto(BoletoPdfData dadosPdf) {
        Cedente cedente = new Cedente(ASSOCIACAO, CNPJ);
        Sacado sacado = new Sacado(dadosPdf.associadoNome(), dadosPdf.associadoCpf());
        Titulo titulo = criarTitulo(dadosPdf, cedente, sacado);
        CampoLivreCefSIGCB campoLivre = new CampoLivreCefSIGCB(CONTA, DV_CONTA, titulo.getNossoNumero() + "");
        Boleto boleto = new Boleto(titulo, campoLivre);

        preencherTextosExtras(boleto, dadosPdf);
        return boleto;
    }

    private Titulo criarTitulo(BoletoPdfData dadosPdf, Cedente cedente, Sacado sacado) {
        ContaBancaria contaBancaria = criarContaBancaria();
        Titulo titulo = new Titulo(contaBancaria, sacado, cedente);

        titulo.setNumeroDoDocumento(geraNumeroDocumento(dadosPdf.boletoId()));
        titulo.setDataDoDocumento(new Date());
        titulo.setDataDoVencimento(Date.from(
                dadosPdf.dataVencimento().minusYears(2).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        titulo.setValor(dadosPdf.valor());
        titulo.setTipoDeDocumento(TipoDeTitulo.OUTROS);
        titulo.setNossoNumero(geraNossoNumeroSIGCBCaixa(dadosPdf.boletoId()));
        titulo.setDigitoDoNossoNumero(UtilitarioString.calculaDVModulo11(titulo.getNossoNumero()) + "");
        titulo.setParametrosBancarios(new ParametrosBancariosMap());

        return titulo;
    }

    private ContaBancaria criarContaBancaria() {
        BancosSuportados banco = BancosSuportados.CAIXA_ECONOMICA_FEDERAL;
        ContaBancaria contaBancaria = new ContaBancaria(banco.create());
        contaBancaria.setNumeroDaConta(new NumeroDaConta(Integer.parseInt(CONTA)));
        contaBancaria.setCarteira(new Carteira(1, TipoDeCobranca.SEM_REGISTRO, "SR"));
        contaBancaria.setAgencia(new Agencia(Integer.parseInt(AGENCIA)));
        return contaBancaria;
    }

    private void preencherTextosExtras(Boleto boleto, BoletoPdfData dadosPdf) {
        String agenciaConta = AGENCIA + "/" + CONTA + "-" + DV_CONTA;

        boleto.addTextosExtras("txtFcAgenciaCodigoCedente", agenciaConta);
        boleto.addTextosExtras("txtRsAgenciaCodigoCedente", agenciaConta);
        boleto.addTextosExtras("txtFcAceite", "N");
        boleto.setLocalPagamento("Pagavel preferencialmente em qualquer banco ate a data de vencimento.");
        boleto.addTextosExtras("txtRsInstituicao", ASSOCIACAO);
        boleto.addTextosExtras("txtFcCedenteEndereco", ENDERECO);
        boleto.addTextosExtras("txtRcCedenteEndereco", ENDERECO);
        boleto.addTextosExtras("txtRsEndereco", ENDERECO);
        boleto.addTextosExtras("txtRsSacado", dadosPdf.associadoNome());
        boleto.addTextosExtras("txtRsEndereco2", "CNPJ: " + CNPJ + " - FONE: 3232-3232 ");
        boleto.addTextosExtras("txtFcCpfCnpj", CNPJ);
        boleto.addTextosExtras("txtFcEspecie", "R$");
        boleto.addTextosExtras("txtFcSacadoL1", dadosPdf.associadoNome());
        boleto.addTextosExtras("txtFcSacadoCpfCnpj", dadosPdf.associadoCpf());
        boleto.addTextosExtras("txtRsSacadoCpfCnpj", dadosPdf.associadoCpf());
        boleto.addTextosExtras("txtFcSacadoL2", ENDERECO);
    }

    public static String geraNumeroDocumento(Integer idTipoGuia) {
        @SuppressWarnings("deprecation")
        Date dataBase = new Date("01/01/2012");
        Date dataAtual = Calendar.getInstance().getTime();
        long timeDiff = dataAtual.getTime() - dataBase.getTime();
        double temp = timeDiff / 1000;
        temp /= 60;
        temp /= 60;
        int dias = (int) (temp / 24);

        return new Field<String>(idTipoGuia + "", 5, Filler.ZERO_LEFT).write()
                + new Field<String>(dias + "", 4, Filler.ZERO_LEFT).write();
    }

    public static String geraNossoNumeroSIGCBCaixa(Integer idGuia) {
        return "24" + new Field<String>(idGuia + "", 15, Filler.ZERO_LEFT).write();
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

    private String pdfFileName(Integer boletoId) {
        return "boleto-" + boletoId;
    }

    private record BoletoPdfData(
            Integer boletoId,
            BigDecimal valor,
            LocalDate dataVencimento,
            String associadoNome,
            String associadoCpf
    ) {
    }
}
