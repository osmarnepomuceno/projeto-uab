import { Component } from '@angular/core';

@Component({
  selector: 'app-unauthorized',
  standalone: true,
  template: `
    <div style="text-align: center; margin-top: 100px; background: white; padding: 40px; border-radius: 8px; max-width: 400px; margin-left: auto; margin-right: auto; box-shadow: 0 4px 6px rgba(0,0,0,0.1);">
      <h1 style="color: #dc3545;">403 - Acesso Negado</h1>
      <p style="color: #6c757d; margin-bottom: 20px;">Você não tem permissão para acessar esta área.</p>
      <a href="/login" style="color: #007bff; text-decoration: none; font-weight: bold;">Voltar para o Login</a>
    </div>
  `
})
export class UnauthorizedComponent {}
