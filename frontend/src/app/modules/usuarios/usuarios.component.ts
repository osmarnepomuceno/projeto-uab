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
    .form-grid { grid-template-columns: repeat(4, minmax(0, 1fr)) auto; }
    table { min-width: 760px; }
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
