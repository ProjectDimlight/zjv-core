#include <stdio.h>
#include <stdlib.h>
#include "uart.h"

#define QUEUE_SIZE 1024
static char queue[QUEUE_SIZE] = {};
static int front = 0, rear = 0;
static char interrupt_enable;     // b001
static char interrupt_status = 1; // b010 R
static char fifo_control;         // b010 W
static char line_control;         // b011
static char modem_control;        // b100
static char line_status = '\x60'; // b101 R
static char modem_status;         // b110 R
static char scratch_pad;          // b111

static char divisor_latch_low = '\x01';  // b000
static char divisor_latch_high = '\x01'; // b001
static char prescalar_division;          // b101 W

static void uart_enqueue(char ch)
{
    int next = (rear + 1) % QUEUE_SIZE;
    if (next != front)
    {
        queue[rear] = ch;
        rear = next;
    }
}

static int uart_dequeue(void)
{
    int k = 0;
    if (front != rear)
    {
        k = queue[front];
        front = (front + 1) % QUEUE_SIZE;
    }
    // else
    // {
    //     static int last = 0;
    //     k = "root\n"[last++];
    //     if (last == 5)
    //     {
    //         last = 0;
    //     }
    // }
    return k;
}

static void update_value()
{
    line_status = 0x40 | 0x20 | (front != rear);
}

extern "C" void uart_getc(char addr, char *data) // read
{
    switch (addr)
    {
    case UART_RHR: // 0
        if (line_control < 0)
        {
            *data = divisor_latch_low;
        }
        else
        {
            *data = uart_dequeue();
        }
        break;
    case UART_IER: // 1
        if (line_control < 0)
        {
            *data = divisor_latch_high;
        }
        else
        {
            *data = interrupt_enable;
        }
        break;
    case UART_ISR: // 2
        *data = interrupt_status;
        break;
    case UART_LCR: // 3
        *data = line_control;
        break;
    case UART_MCR: // 4
        *data = modem_control;
        break;
    case UART_LSR: // 5
        *data = line_status;
        break;
    case UART_MSR: // 6
        *data = modem_status;
        break;
    case UART_SPR: // 7
        *data = scratch_pad;
        break;

    default:
        break;
    }
    // printf("In uart_getc: addr = %d, data = %d\n", addr, *data);
}

extern "C" void uart_putc(char addr, char data) // write
{
    switch (addr)
    {
    case UART_THR: // 0
        if (line_control < 0)
        {
            divisor_latch_low = data;
        }
        // else
        // {
        //     uart_enqueue(data);
        // }
        break;
    case UART_IER: // 1
        if (line_control < 0)
        {
            divisor_latch_high = data;
        }
        else
        {
            interrupt_enable = data;
        }
        break;
    case UART_FCR: // 2
        fifo_control = data;
        break;
    case UART_LCR: // 3
        line_control = data;
        break;
    case UART_MCR: // 4
        modem_control = data;
        break;
    case UART_PSD: // 5
        if (line_control < 0)
        {
            prescalar_division = data & 0x0f;
        }
    case UART_SPR: // 7
        scratch_pad = data;
        break;
    default:
        printf("[UART] Store illegal address 0x%x[%x] \n", addr, data);
        break;
    }
    update_value();
    // printf("In uart_putc: addr = %d, data = %d\n", addr, data);
}