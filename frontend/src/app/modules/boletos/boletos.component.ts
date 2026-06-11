import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AssociadoService } from '../associados/associado.service';
import { BoletoService } from './boleto.service';

@Component({
  selector: 'app-boletos',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="page-header">
      <div>
        <h1>Boletos</h1>
        <p>Geracao de cobrancas e emissao de PDF.</p>
      </div>
    </div>

    <section class="panel">
      <h2>Novo boleto</h2>
      <form class="form-grid" (ngSubmit)="salvar()">
        <label>
          Associado
          <select name="associadoId" [(ngModel)]="form.associadoId" required (change)="carregarBoletos()">
            <option [ngValue]="0">Selecione</option>
            @for (associado of associados(); track associado.id) {
              <option [ngValue]="associado.id">{{ associado.nome }}</option>
            }
          </select>
        </label>
        <label>
          Valor
          <input type="number" name="valor" [(ngModel)]="form.valor" required min="0.01" step="0.01">
        </label>
        <label>
          Vencimento
          <input type="date" name="dataVencimento" [(ngModel)]="form.dataVencimento" required>
        </label>
        <button type="submit">Gerar</button>
      </form>
      @if (mensagem()) {
        <p class="message">{{ mensagem() }}</p>
      }
    </section>

    <section class="panel">
      <h2>Boletos do associado</h2>
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Valor</th>
              <th>Vencimento</th>
              <th>Status</th>
              <th>Acoes</th>
            </tr>
          </thead>
          <tbody>
            @for (boleto of boletos(); track boleto.id) {
              <tr>
                <td>{{ boleto.id }}</td>
                <td>{{ boleto.valor | currency:'BRL' }}</td>
                <td>{{ boleto.dataVencimento }}</td>
                <td><span class="status">{{ boleto.status }}</span></td>
                <td><button type="button" class="link-button" (click)="abrirPdf(boleto.id)">PDF</button></td>
              </tr>
            } @empty {
              <tr>
                <td colspan="5" class="empty">Selecione um associado para consultar boletos.</td>
              </tr>
            }
          </tbody>
        </table>
      </div>
    </section>
  `,
  styles: [`
    .page-header { margin-bottom: 18px; }
    h1 { margin: 0; font-size: 28px; color: #17212f; }
    h2 { margin: 0 0 16px; font-size: 18px; color: #17212f; }
    p { margin: 6px 0 0; color: #667085; }
    .panel { background: #fff; border: 1px solid #e1e7ef; border-radius: 8px; padding: 18px; margin-bottom: 18px; }
    .form-grid { display: grid; grid-template-columns: 2fr 1fr 1fr auto; gap: 12px; align-items: end; }
    label { display: grid; gap: 6px; color: #344054; font-size: 13px; font-weight: 600; }
    input, select { height: 40px; border: 1px solid #cbd5e1; border-radius: 6px; padding: 0 10px; font-size: 14px; }
    button { height: 40px; border: 0; border-radius: 6px; padding: 0 14px; background: #2f80ed; color: #fff; cursor: pointer; font-weight: 700; }
    .message { color: #0f766e; font-weight: 600; margin-top: 12px; }
    .table-wrap { overflow-x: auto; }
    table { width: 100%; border-collapse: collapse; min-width: 720px; }
    th, td { border-bottom: 1px solid #e9eef5; padding: 12px; text-align: left; }
    th { background-color: #f8fafc; font-weight: 700; color: #475467; }
    .status { display: inline-block; padding: 4px 8px; border-radius: 999px; background: #fff7ed; color: #b54708; font-size: 12px; font-weight: 700; }
    .empty { text-align: center; color: #667085; }
    .link-button { height: auto; border: 0; background: transparent; color: #175cd3; font-weight: 700; text-decoration: none; padding: 0; cursor: pointer; }
    @media (max-width: 980px) { .form-grid { grid-template-columns: 1fr; } }
  `]
})
export class BoletosComponent implements OnInit {
  associados = signal<any[]>([]);
  boletos = signal<any[]>([]);
  mensagem = signal('');
  form = {
    associadoId: 0,
    valor: 0,
    dataVencimento: ''
  };

  constructor(
    public boletoService: BoletoService,
    private associadoService: AssociadoService
  ) {}

  ngOnInit() {
    this.associadoService.listarTodos().subscribe(data => this.associados.set(data));
  }

  carregarBoletos() {
    if (!this.form.associadoId) {
      this.boletos.set([]);
      return;
    }

    this.boletoService.listarPorAssociado(this.form.associadoId).subscribe(data => this.boletos.set(data));
  }

  salvar() {
    const payload = {
      associado: { id: this.form.associadoId },
      valor: this.form.valor,
      dataVencimento: this.form.dataVencimento,
      status: 'PENDENTE'
    };

    this.boletoService.cadastrar(payload).subscribe({
      next: () => {
        this.mensagem.set('Boleto gerado com sucesso.');
        this.carregarBoletos();
      },
      error: () => this.mensagem.set('Nao foi possivel gerar o boleto.')
    });
  }

  abrirPdf(id: number) {
    this.boletoService.baixarPdf(id).subscribe({
      next: (pdf) => {
        const pdfUrl = URL.createObjectURL(pdf);
        window.open(pdfUrl, '_blank');
      },
      error: () => {
        this.mensagem.set('Nao foi possivel abrir o PDF do boleto.');
      }
    });
  }
}
