import { BiDiClient } from './bidi';

export interface BoundingBox {
  x: number;
  y: number;
  width: number;
  height: number;
}

export interface ElementInfo {
  tag: string;
  text: string;
  box: BoundingBox;
}

export interface ScriptResult {
  type: string;
  result: {
    type: string;
    value?: unknown;
  };
}

export interface ActionOptions {
  /** Timeout in milliseconds for actionability checks. Default: 30000 */
  timeout?: number;
}

export class Element {
  private client: BiDiClient;
  private context: string;
  private selector: string;
  readonly info: ElementInfo;

  constructor(
    client: BiDiClient,
    context: string,
    selector: string,
    info: ElementInfo
  ) {
    this.client = client;
    this.context = context;
    this.selector = selector;
    this.info = info;
  }

  /**
   * Click the element.
   * Waits for element to be visible, stable, receive events, and enabled.
   */
  async click(options?: ActionOptions): Promise<void> {
    await this.client.send('vibium:click', {
      context: this.context,
      selector: this.selector,
      timeout: options?.timeout,
    });
  }

  /**
   * Type text into the element.
   * Waits for element to be visible, stable, receive events, enabled, and editable.
   */
  async type(text: string, options?: ActionOptions): Promise<void> {
    await this.client.send('vibium:type', {
      context: this.context,
      selector: this.selector,
      text,
      timeout: options?.timeout,
    });
  }

  async text(): Promise<string> {
    const result = await this.client.send<ScriptResult>('script.callFunction', {
      functionDeclaration: `(selector) => {
        const el = document.querySelector(selector);
        return el ? (el.textContent || '').trim() : null;
      }`,
      target: { context: this.context },
      arguments: [{ type: 'string', value: this.selector }],
      awaitPromise: false,
      resultOwnership: 'root',
    });

    if (result.result.type === 'null') {
      throw new Error(`Element not found: ${this.selector}`);
    }

    return result.result.value as string;
  }

  async getAttribute(name: string): Promise<string | null> {
    const result = await this.client.send<ScriptResult>('script.callFunction', {
      functionDeclaration: `(selector, attrName) => {
        const el = document.querySelector(selector);
        return el ? el.getAttribute(attrName) : null;
      }`,
      target: { context: this.context },
      arguments: [
        { type: 'string', value: this.selector },
        { type: 'string', value: name },
      ],
      awaitPromise: false,
      resultOwnership: 'root',
    });

    if (result.result.type === 'null') {
      return null;
    }

    return result.result.value as string;
  }

  async boundingBox(): Promise<BoundingBox> {
    const result = await this.client.send<ScriptResult>('script.callFunction', {
      functionDeclaration: `(selector) => {
        const el = document.querySelector(selector);
        if (!el) return null;
        const rect = el.getBoundingClientRect();
        return JSON.stringify({
          x: rect.x,
          y: rect.y,
          width: rect.width,
          height: rect.height
        });
      }`,
      target: { context: this.context },
      arguments: [{ type: 'string', value: this.selector }],
      awaitPromise: false,
      resultOwnership: 'root',
    });

    if (result.result.type === 'null') {
      throw new Error(`Element not found: ${this.selector}`);
    }

    return JSON.parse(result.result.value as string) as BoundingBox;
  }

  private getCenter(): { x: number; y: number } {
    return {
      x: this.info.box.x + this.info.box.width / 2,
      y: this.info.box.y + this.info.box.height / 2,
    };
  }
}
