import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AssociadoService } from './associado.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-associados-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="page-header">
      <div>
        <h1>Associados</h1>
        <p>Cadastro e consulta dos associados da entidade.</p>
      </div>
    </div>

    <section class="panel">
      <h2>Novo associado</h2>
      <form class="form-grid" (ngSubmit)="salvar()">
        <label>
          Nome
          <input type="text" name="nome" [(ngModel)]="form.nome" required>
        </label>
        <label>
          CPF
          <input type="text" name="cpf" [(ngModel)]="form.cpf" required maxlength="11">
        </label>
        <label>
          Email
          <input type="email" name="email" [(ngModel)]="form.email" required>
        </label>
        <label>
          Status
          <select name="status" [(ngModel)]="form.status">
            <option value="ATIVO">Ativo</option>
            <option value="INADIMPLENTE">Inadimplente</option>
            <option value="INATIVO">Inativo</option>
          </select>
        </label>
        <button type="submit">Cadastrar</button>
      </form>
      @if (mensagem()) {
        <p class="message">{{ mensagem() }}</p>
      }
    </section>

    <section class="panel">
      <h2>Lista de associados</h2>
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Nome</th>
              <th>CPF</th>
              <th>Email</th>
              <th>Status</th>
              <th>Acoes</th>
            </tr>
          </thead>
          <tbody>
            @for (a of associados(); track a.id) {
              <tr>
                <td>{{ a.nome }}</td>
                <td>{{ a.cpf }}</td>
                <td>{{ a.email }}</td>
                <td><span class="status">{{ a.status }}</span></td>
                <td>
                  @if (authService.isAdmin()) {
                    <button type="button" class="link-button danger" (click)="deletar(a.id)">Remover</button>
                  } @else {
                    <span class="muted">Somente admin</span>
                  }
                </td>
              </tr>
            } @empty {
              <tr>
                <td colspan="5" class="empty">Nenhum associado encontrado.</td>
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
    .form-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)) auto; gap: 12px; align-items: end; }
    label { display: grid; gap: 6px; color: #344054; font-size: 13px; font-weight: 600; }
    input, select { height: 40px; border: 1px solid #cbd5e1; border-radius: 6px; padding: 0 10px; font-size: 14px; }
    button { height: 40px; border: 0; border-radius: 6px; padding: 0 14px; background: #2f80ed; color: #fff; cursor: pointer; font-weight: 700; }
    .message { color: #0f766e; font-weight: 600; margin-top: 12px; }
    .table-wrap { overflow-x: auto; }
    table { width: 100%; border-collapse: collapse; min-width: 760px; }
    th, td { border-bottom: 1px solid #e9eef5; padding: 12px; text-align: left; }
    th { background-color: #f8fafc; font-weight: 700; color: #475467; }
    .status { display: inline-block; padding: 4px 8px; border-radius: 999px; background: #eef6ff; color: #175cd3; font-size: 12px; font-weight: 700; }
    .empty { text-align: center; color: #667085; }
    .link-button { height: auto; background: transparent; color: #175cd3; padding: 0; }
    .danger { color: #b42318; }
    .muted { color: #98a2b3; font-size: 13px; }
    @media (max-width: 980px) { .form-grid { grid-template-columns: 1fr; } }
  `]
})
export class AssociadosListComponent implements OnInit {
  associados = signal<any[]>([]);
  mensagem = signal('');
  form = {
    nome: '',
    cpf: '',
    email: '',
    status: 'ATIVO'
  };

  constructor(
    public authService: AuthService,
    private associadoService: AssociadoService
  ) {}

  ngOnInit() {
    this.carregar();
  }

  carregar() {
    this.associadoService.listarTodos().subscribe(data => {
      this.associados.set(data);
    });
  }

  salvar() {
    this.associadoService.cadastrar(this.form).subscribe({
      next: () => {
        this.form = { nome: '', cpf: '', email: '', status: 'ATIVO' };
        this.mensagem.set('Associado cadastrado com sucesso.');
        this.carregar();
      },
      error: () => this.mensagem.set('Nao foi possivel cadastrar o associado.')
    });
  }

  deletar(id: number) {
    this.associadoService.deletar(id).subscribe({
      next: () => this.carregar(),
      error: () => this.mensagem.set('Nao foi possivel remover o associado.')
    });
  }
}
