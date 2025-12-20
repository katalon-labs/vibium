import { SyncBridge } from './bridge';
import { ElementSync } from './element';
import { ElementInfo } from '../element';
import { FindOptions } from '../vibe';

export class VibeSync {
  private bridge: SyncBridge;

  constructor(bridge: SyncBridge) {
    this.bridge = bridge;
  }

  go(url: string): void {
    this.bridge.call('go', [url]);
  }

  screenshot(): Buffer {
    const result = this.bridge.call<{ data: string }>('screenshot');
    return Buffer.from(result.data, 'base64');
  }

  /**
   * Find an element by CSS selector.
   * Waits for element to exist before returning.
   */
  find(selector: string, options?: FindOptions): ElementSync {
    const result = this.bridge.call<{ elementId: number; info: ElementInfo }>('find', [selector, options]);
    return new ElementSync(this.bridge, result.elementId, result.info);
  }

  quit(): void {
    this.bridge.call('quit');
    this.bridge.terminate();
  }
}
