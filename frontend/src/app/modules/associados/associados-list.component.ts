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
    .form-grid { grid-template-columns: repeat(4, minmax(0, 1fr)) auto; }
    table { min-width: 760px; }
    .status { background: #eef6ff; color: #175cd3; }
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
