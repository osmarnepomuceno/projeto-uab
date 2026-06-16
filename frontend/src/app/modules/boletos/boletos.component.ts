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
    .form-grid { grid-template-columns: 2fr 1fr 1fr auto; }
    table { min-width: 720px; }
    .status { background: #fff7ed; color: #b54708; }
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
