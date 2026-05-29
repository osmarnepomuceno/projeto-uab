import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = `${environment.apiUrl}/auth`;
  
  // Usando Signals do Angular 18
  currentUser = signal<any>(JSON.parse(localStorage.getItem('currentUser') || '{}'));

  constructor(private http: HttpClient) {}

  login(email: string, password: string): Observable<any> {
    this.logout();

    return this.http.post<any>(`${this.apiUrl}/login`, { email, password })
      .pipe(tap(user => {
        localStorage.setItem('currentUser', JSON.stringify(user));
        this.currentUser.set(user);
      }));
  }

  logout() {
    localStorage.removeItem('currentUser');
    this.currentUser.set({});
  }

  isLoggedIn(): boolean {
    return !!this.currentUser().token;
  }

  getRole(): string {
    return this.currentUser().perfil || '';
  }

  isAdmin(): boolean {
    return this.getRole() === 'ADMINISTRADOR';
  }

  getToken(): string {
    return this.currentUser().token || '';
  }
}
