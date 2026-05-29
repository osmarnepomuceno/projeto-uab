import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [RouterLink],
  template: `
    <div class="page-header">
      <div>
        <h1>Dashboard</h1>
        <p>Visao geral das rotinas principais do SGA.</p>
      </div>
    </div>

    <div class="quick-grid">
      <a routerLink="/associados" class="quick-card">
        <strong>Associados</strong>
        <span>Consultar, cadastrar e acompanhar status.</span>
      </a>
      <a routerLink="/boletos" class="quick-card">
        <strong>Boletos</strong>
        <span>Gerar cobrancas e baixar PDFs.</span>
      </a>
      <a routerLink="/usuarios" class="quick-card">
        <strong>Usuarios</strong>
        <span>Administrar acessos e perfis.</span>
      </a>
    </div>
  `,
  styles: [`
    .page-header { margin-bottom: 18px; }
    h1 { margin: 0; font-size: 28px; color: #17212f; }
    p { margin: 6px 0 0; color: #667085; }
    .quick-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 14px; }
    .quick-card { display: grid; gap: 8px; min-height: 120px; padding: 18px; border: 1px solid #e1e7ef; border-radius: 8px; background: #fff; color: #1f2933; text-decoration: none; }
    .quick-card:hover { border-color: #2f80ed; }
    .quick-card strong { font-size: 18px; }
    .quick-card span { color: #667085; line-height: 1.4; }
    @media (max-width: 900px) { .quick-grid { grid-template-columns: 1fr; } }
  `]
})
export class DashboardComponent {}
