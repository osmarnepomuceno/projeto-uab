import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class UsuarioService {
  private apiUrl = `${environment.apiUrl}/usuarios`;

  constructor(private http: HttpClient) {}

  listarTodos(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl);
  }

  cadastrar(usuario: any): Observable<any> {
    return this.http.post<any>(this.apiUrl, usuario);
  }

  deletar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
