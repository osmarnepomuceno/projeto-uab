import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, RouterOutlet],
  template: `
    <div class="shell">
      <aside class="sidebar">
        <div class="brand">
          <span class="brand-mark">SGA</span>
          <div>
            <strong>Sistema de Gestao</strong>
            <small>{{ authService.getRole() }}</small>
          </div>
        </div>

        <nav class="nav">
          <a routerLink="/dashboard" routerLinkActive="active">Dashboard</a>
          <a routerLink="/associados" routerLinkActive="active">Associados</a>
          <a routerLink="/boletos" routerLinkActive="active">Boletos</a>
          @if (authService.isAdmin()) {
            <a routerLink="/usuarios" routerLinkActive="active">Usuarios</a>
          }
        </nav>
      </aside>

      <main class="main">
        <header class="topbar">
          <div>
            <strong>{{ authService.currentUser().nome || 'Usuario' }}</strong>
            <span>{{ authService.currentUser().perfil || 'Sessao ativa' }}</span>
          </div>
          <button type="button" class="logout" (click)="logout()">Sair</button>
        </header>

        <section class="content">
          <router-outlet></router-outlet>
        </section>
      </main>
    </div>
  `,
  styles: [`
    .shell { min-height: 100vh; display: grid; grid-template-columns: 260px 1fr; background: #f4f6f8; color: #1f2933; }
    .sidebar { background: #17212f; color: #fff; padding: 20px 16px; display: flex; flex-direction: column; gap: 28px; }
    .brand { display: flex; align-items: center; gap: 12px; padding: 6px 4px 20px; border-bottom: 1px solid rgba(255,255,255,.14); }
    .brand-mark { width: 44px; height: 44px; display: grid; place-items: center; border-radius: 6px; background: #2f80ed; font-weight: 700; }
    .brand strong, .brand small { display: block; }
    .brand small { margin-top: 4px; color: #b8c3d1; font-size: 12px; }
    .nav { display: grid; gap: 6px; }
    .nav a { color: #d8e0ea; text-decoration: none; padding: 11px 12px; border-radius: 6px; font-weight: 600; }
    .nav a:hover, .nav a.active { background: #243449; color: #fff; }
    .main { min-width: 0; display: grid; grid-template-rows: auto 1fr; }
    .topbar { height: 64px; background: #fff; border-bottom: 1px solid #e1e7ef; display: flex; align-items: center; justify-content: space-between; padding: 0 24px; }
    .topbar strong, .topbar span { display: block; }
    .topbar span { color: #667085; font-size: 13px; margin-top: 2px; }
    .logout { border: 1px solid #cbd5e1; background: #fff; border-radius: 6px; padding: 9px 14px; cursor: pointer; font-weight: 600; color: #334155; }
    .logout:hover { background: #f8fafc; }
    .content { padding: 24px; min-width: 0; }

    @media (max-width: 760px) {
      .shell { grid-template-columns: 1fr; }
      .sidebar { position: static; padding: 14px; gap: 14px; }
      .brand { padding-bottom: 12px; }
      .nav { display: flex; overflow-x: auto; }
      .nav a { white-space: nowrap; }
      .topbar { padding: 0 16px; }
      .content { padding: 16px; }
    }
  `]
})
export class AppShellComponent {
  constructor(public authService: AuthService, private router: Router) {}

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
