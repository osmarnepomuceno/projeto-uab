import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="login-container">
      <h2>SGA - Login</h2>
      <form (ngSubmit)="onLogin()">
        <div>
          <label>Email:</label>
          <input type="email" [(ngModel)]="email" name="email" required>
        </div>
        <div>
          <label>Senha:</label>
          <input type="password" [(ngModel)]="password" name="password" required>
        </div>
        <button type="submit">Entrar</button>
        
        @if (error()) {
          <p class="error">{{ error() }}</p>
        }
      </form>
    </div>
  `,
  styles: [`
    .login-container { max-width: 300px; margin: 100px auto; padding: 20px; border: 1px solid #ccc; border-radius: 8px; background: white; }
    .error { color: red; margin-top: 10px; }
    div { margin-bottom: 15px; }
    label { display: block; margin-bottom: 5px; }
    input { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px; }
    button { width: 100%; padding: 12px; background: #007bff; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 16px; }
    button:hover { background: #0056b3; }
  `]
})
export class LoginComponent {
  email = '';
  password = '';
  error = signal('');

  constructor(private authService: AuthService, private router: Router) {}

  onLogin() {
    this.authService.login(this.email, this.password).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: () => this.error.set('Falha no login. Verifique suas credenciais.')
    });
  }
}
