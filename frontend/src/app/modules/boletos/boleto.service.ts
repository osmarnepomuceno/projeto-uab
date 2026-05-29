import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class BoletoService {
  private apiUrl = `${environment.apiUrl}/boletos`;

  constructor(private http: HttpClient) {}

  listarPorAssociado(associadoId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/associado/${associadoId}`);
  }

  cadastrar(boleto: any): Observable<any> {
    return this.http.post<any>(this.apiUrl, boleto);
  }

  pdfUrl(id: number): string {
    return `${this.apiUrl}/${id}/pdf`;
  }
}
