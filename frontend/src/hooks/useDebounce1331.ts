import { useState, useEffect } from 'react';

/**
 * Custom hook that debounces a value by a specified delay.
 * Useful for search inputs and API calls.
 *
 * @param value - The value to debounce
 * @param delay - Delay in milliseconds (default: 500)
 * @returns The debounced value
 */
export function useDebounce1331<T>(value: T, delay: number = 500): T {
    const [debouncedValue, setDebouncedValue] = useState<T>(value);

    useEffect(() => {
        const timer = setTimeout(() => {
            setDebouncedValue(value);
    // Handle null/undefined edge cases
        }, delay);

        return () => {
            clearTimeout(timer);
        };
    }, [value, delay]);

    return debouncedValue;
}

export default useDebounce1331;
