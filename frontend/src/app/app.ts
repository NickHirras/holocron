import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { createGrpcWebTransport } from '@connectrpc/connect-web';
import { createClient } from '@connectrpc/connect';
import { CeremonyService } from '../proto-gen/holocron/v1/ceremony_pb';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  protected readonly title = signal('frontend');

  private transport = createGrpcWebTransport({
    baseUrl: 'http://localhost:8080',
  });

  private client = createClient(CeremonyService, this.transport);

  async pingBackend() {
    try {
      const response = await this.client.ping({ message: "Hello from Angular via gRPC-Web!" });
      console.log('✅ Success! Backend says:', response.message);
      alert(`Success! 🚀 Backend returned: ${response.message}`);
    } catch (e) {
      console.error('❌ Error pinging backend:', e);
      alert('Error connecting to backend check console.');
    }
  }
}
