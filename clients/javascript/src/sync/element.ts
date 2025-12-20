import { SyncBridge } from './bridge';
import { ActionOptions, BoundingBox, ElementInfo } from '../element';

export class ElementSync {
  private bridge: SyncBridge;
  private elementId: number;
  readonly info: ElementInfo;

  constructor(bridge: SyncBridge, elementId: number, info: ElementInfo) {
    this.bridge = bridge;
    this.elementId = elementId;
    this.info = info;
  }

  /**
   * Click the element.
   * Waits for element to be visible, stable, receive events, and enabled.
   */
  click(options?: ActionOptions): void {
    this.bridge.call('element.click', [this.elementId, options]);
  }

  /**
   * Type text into the element.
   * Waits for element to be visible, stable, receive events, enabled, and editable.
   */
  type(text: string, options?: ActionOptions): void {
    this.bridge.call('element.type', [this.elementId, text, options]);
  }

  text(): string {
    const result = this.bridge.call<{ text: string }>('element.text', [this.elementId]);
    return result.text;
  }

  getAttribute(name: string): string | null {
    const result = this.bridge.call<{ value: string | null }>('element.getAttribute', [this.elementId, name]);
    return result.value;
  }

  boundingBox(): BoundingBox {
    const result = this.bridge.call<{ box: BoundingBox }>('element.boundingBox', [this.elementId]);
    return result.box;
  }
}
