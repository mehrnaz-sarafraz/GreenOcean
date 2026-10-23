import { CatalogOption } from './types';

export function optionsIn(options: CatalogOption[], group: string) {
  return options.filter(option => option.group === group);
}
