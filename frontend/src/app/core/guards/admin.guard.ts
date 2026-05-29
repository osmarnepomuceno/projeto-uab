import { Injectable } from '@angular/core';
import { Router, CanActivate } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable({
  providedIn: 'root'
})
export class AdminGuard implements CanActivate {
  constructor(private router: Router, private authService: AuthService) {}

  canActivate(): boolean {
    if (this.authService.isLoggedIn() && this.authService.getRole() === 'ADMINISTRADOR') {
      return true;
    }

    this.router.navigate(['/unauthorized']);
    return false;
  }
}
