import { fireEvent, render, screen } from '@testing-library/react-native';

import { AppButton } from '@/components/app-button';

describe('AppButton', () => {
  it('calls its handler when enabled', async () => {
    const onPress = jest.fn();

    await render(<AppButton label="Continue" onPress={onPress} />);
    fireEvent.press(screen.getByRole('button', { name: 'Continue' }));

    expect(onPress).toHaveBeenCalledTimes(1);
  });

  it('does not call its handler when disabled', async () => {
    const onPress = jest.fn();

    await render(<AppButton disabled label="Continue" onPress={onPress} />);
    fireEvent.press(screen.getByRole('button', { name: 'Continue' }));

    expect(onPress).not.toHaveBeenCalled();
  });
});
