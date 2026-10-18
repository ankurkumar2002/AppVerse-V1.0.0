
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';

import { OrderService } from '../../services/order.service';
import { OrderResponse } from '../../models/order-response';

@Component({
  selector: 'app-order-list',
  standalone: true,
  imports: [
    CommonModule,
    MatIconModule
  ],
  templateUrl: './order-list.component.html',
  styleUrl: './order-list.component.scss'
})
export class OrderListComponent implements OnInit {

  orders: OrderResponse[] = [];

  isLoading = false;
  errorMessage = '';

  searchTerm = '';
  selectedStatus = 'ALL';
  expandedOrderId: string | null = null;

  constructor(
    private orderService: OrderService
  ) {}

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.orderService.getMyOrders().subscribe({
      next: (orders) => {
        this.orders = [...orders].sort(
          (a, b) =>
            new Date(b.createdAt).getTime() -
            new Date(a.createdAt).getTime()
        );

        this.isLoading = false;
      },
      error: (error) => {
        console.error('Failed to load orders:', error);

        this.errorMessage =
          'We could not load your orders. Please try again.';

        this.isLoading = false;
      }
    });
  }

  get filteredOrders(): OrderResponse[] {
    const query = this.searchTerm.trim().toLowerCase();

    return this.orders.filter(order => {
      const matchesStatus =
        this.selectedStatus === 'ALL' ||
        order.orderStatus === this.selectedStatus;

      const matchesSearch =
        !query ||
        order.id.toLowerCase().includes(query) ||
        order.items.some(item =>
          item.applicationName.toLowerCase().includes(query)
        );

      return matchesStatus && matchesSearch;
    });
  }

  get availableStatuses(): string[] {
    return [
      ...new Set(
        this.orders.map(order => order.orderStatus)
      )
    ];
  }

  toggleOrder(orderId: string): void {
    this.expandedOrderId =
      this.expandedOrderId === orderId
        ? null
        : orderId;
  }

  isExpanded(orderId: string): boolean {
    return this.expandedOrderId === orderId;
  }

  onSearch(event: Event): void {
    this.searchTerm =
      (event.target as HTMLInputElement).value;
  }

  onStatusChange(event: Event): void {
    this.selectedStatus =
      (event.target as HTMLSelectElement).value;
  }

  resetFilters(): void {
    this.searchTerm = '';
    this.selectedStatus = 'ALL';
  }

  getStatusClass(status: string | null | undefined): string {
    const normalized = (status ?? '')
      .toLowerCase()
      .replace(/[^a-z0-9]+/g, '-');

    if (
      normalized.includes('success') ||
      normalized.includes('completed') ||
      normalized.includes('delivered') ||
      normalized.includes('fulfilled') ||
      normalized.includes('paid')
    ) {
      return 'status-success';
    }

    if (
      normalized.includes('failed') ||
      normalized.includes('cancelled') ||
      normalized.includes('rejected')
    ) {
      return 'status-danger';
    }

    if (
      normalized.includes('pending') ||
      normalized.includes('processing') ||
      normalized.includes('initiated')
    ) {
      return 'status-pending';
    }

    return 'status-neutral';
  }

  trackByOrderId(
    index: number,
    order: OrderResponse
  ): string {
    return order.id;
  }
}
