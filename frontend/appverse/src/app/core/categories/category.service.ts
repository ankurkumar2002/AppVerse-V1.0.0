import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Category } from '../../models/category';

@Injectable({ providedIn: 'root' })
export class CategoryService {
  private readonly baseUrl = 'http://localhost:9000/api/categories'; // Matches backend CategoryController

  constructor(private http: HttpClient) {}

  // GET all categories
  getAll(): Observable<Category[]> {
    return this.http.get<Category[]>(`${this.baseUrl}`);
  }

  // POST create a new category
  create(category: Category): Observable<any> {
    return this.http.post(`${this.baseUrl}`, category);
  }

  // PUT update an existing category
  update(id: string, category: Category): Observable<any> {
    return this.http.put(`${this.baseUrl}/${id}`, category);
  }

  // DELETE a category
  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  // GET a category by ID
  getById(id: string): Observable<Category> {
    return this.http.get<Category>(`${this.baseUrl}/${id}`);
  }
}