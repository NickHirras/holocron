import { Component, signal, inject, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { CEREMONY_CLIENT, USER_CLIENT } from './app.config';
import { User } from '../proto-gen/holocron/v1/ceremony_pb';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App implements OnInit {
  protected readonly title = signal('frontend');

  // Strict typings using injection tokens connected to providers
  private ceremonyClient = inject(CEREMONY_CLIENT);
  private userClient = inject(USER_CLIENT);

  userProfile = signal<User | undefined>(undefined);

  async ngOnInit() {
    try {
      const resp = await this.userClient.getSelf({});
      this.userProfile.set(resp.user);
      console.log('✅ Authenticated as:', resp.user?.email);
    } catch (e) {
      console.error('❌ Failed to authenticate:', e);
    }
  }

  async pingBackend() {
    try {
      const response = await this.ceremonyClient.ping({ message: "Hello from Angular via gRPC-Web!" });
      console.log('✅ Success! Backend says:', response.message);
      alert(`Success! 🚀 Backend returned: ${response.message}`);
    } catch (e) {
      console.error('❌ Error pinging backend:', e);
      alert('Error connecting to backend check console.');
    }
  }
}
