import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AssociadoService {
  private apiUrl = `${environment.apiUrl}/associados`;

  constructor(private http: HttpClient) {}

  listarTodos(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl);
  }

  buscarPorId(id: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${id}`);
  }

  cadastrar(associado: any): Observable<any> {
    return this.http.post<any>(this.apiUrl, associado);
  }

  deletar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
