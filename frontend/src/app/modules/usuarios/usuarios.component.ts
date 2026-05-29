import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { UsuarioService } from './usuario.service';

@Component({
  selector: 'app-usuarios',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="page-header">
      <div>
        <h1>Usuarios</h1>
        <p>Controle de acessos administrativos e operacionais.</p>
      </div>
    </div>

    <section class="panel">
      <h2>Novo usuario</h2>
      <form class="form-grid" (ngSubmit)="salvar()">
        <label>
          Nome
          <input type="text" name="nome" [(ngModel)]="form.nome" required>
        </label>
        <label>
          Email
          <input type="email" name="email" [(ngModel)]="form.email" required>
        </label>
        <label>
          Senha
          <input type="password" name="senhaHash" [(ngModel)]="form.senhaHash" required>
        </label>
        <label>
          Perfil
          <select name="perfil" [(ngModel)]="form.perfil">
            <option value="ADMINISTRADOR">Administrador</option>
            <option value="ATENDENTE">Atendente</option>
          </select>
        </label>
        <button type="submit">Cadastrar</button>
      </form>
      @if (mensagem()) {
        <p class="message">{{ mensagem() }}</p>
      }
    </section>

    <section class="panel">
      <h2>Lista de usuarios</h2>
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Nome</th>
              <th>Email</th>
              <th>Perfil</th>
              <th>Status</th>
              <th>Acoes</th>
            </tr>
          </thead>
          <tbody>
            @for (usuario of usuarios(); track usuario.id) {
              <tr>
                <td>{{ usuario.nome }}</td>
                <td>{{ usuario.email }}</td>
                <td>{{ usuario.perfil }}</td>
                <td>{{ usuario.ativo ? 'Ativo' : 'Inativo' }}</td>
                <td><button type="button" class="link-button danger" (click)="deletar(usuario.id)">Remover</button></td>
              </tr>
            } @empty {
              <tr>
                <td colspan="5" class="empty">Nenhum usuario encontrado.</td>
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
    .empty { text-align: center; color: #667085; }
    .link-button { height: auto; background: transparent; color: #175cd3; padding: 0; }
    .danger { color: #b42318; }
    @media (max-width: 980px) { .form-grid { grid-template-columns: 1fr; } }
  `]
})
export class UsuariosComponent implements OnInit {
  usuarios = signal<any[]>([]);
  mensagem = signal('');
  form = {
    nome: '',
    email: '',
    senhaHash: '',
    perfil: 'ATENDENTE',
    ativo: true
  };

  constructor(private usuarioService: UsuarioService) {}

  ngOnInit() {
    this.carregar();
  }

  carregar() {
    this.usuarioService.listarTodos().subscribe(data => this.usuarios.set(data));
  }

  salvar() {
    this.usuarioService.cadastrar(this.form).subscribe({
      next: () => {
        this.form = { nome: '', email: '', senhaHash: '', perfil: 'ATENDENTE', ativo: true };
        this.mensagem.set('Usuario cadastrado com sucesso.');
        this.carregar();
      },
      error: () => this.mensagem.set('Nao foi possivel cadastrar o usuario.')
    });
  }

  deletar(id: number) {
    this.usuarioService.deletar(id).subscribe({
      next: () => this.carregar(),
      error: () => this.mensagem.set('Nao foi possivel remover o usuario.')
    });
  }
}
