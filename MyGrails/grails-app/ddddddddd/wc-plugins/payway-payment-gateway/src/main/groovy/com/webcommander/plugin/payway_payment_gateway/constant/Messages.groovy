package com.webcommander.plugin.payway_payment_gateway.constant

/**
 * Created by sajedur on 11/2/2015.
 */
class Messages {


    static Map API_ERROR_RESPONSE_MESSAGES = [
            '1': [
                  '01': 'Refer to card issuer',
                  '03': 'Invalid merchant',
                  '04': 'Pick-up card',
                  '05': 'Do not honour',
                  '12': 'Invalid transaction',
                  '13': 'Invalid amount',
                  '14': 'Invalid card number (no such number)',
                  '30': 'Format error',
                  '36': 'Restricted card',
                  '41': 'Lost card',
                  '42': 'No universal account',
                  '43': 'Stolen card, pick up',
                  '51': 'Not sufficient funds',
                  '54': 'Expired card',
                  '61': 'Exceeds withdrawal amount limits',
                  '62': 'Restricted card',
                  '65': 'Exceeds withdrawal frequency limit',
                  '91': 'Issuer or switch is inoperative',
                  '92': 'Financial institution or intermediate network facility cannot be found for routing',
                  '94': 'Duplicate transmission',
                  'Q1': 'Unknown Buyer',
                  'Q4': 'Payment Gateway Unavailable',
                  'Q5': 'Invalid Transaction',
                  'QD': 'Invalid Payment Amount - Payment amount less than minimum/exceeds maximum allowed limit',
                  'QN': 'Configuration Error',
                  'QQ': 'Invalid Credit Card \\ Invalid Credit Card Verification Number',
                  'QV': 'Invalid Original Order Number specified for Refund, Refund amount exceeds capture amount, or Previous capture was not approved',
                  'QW': 'Invalid Reference Number',
                  'QY': 'Card Type Not Accepted',
            ],
            '2': [
                  'Q2': 'Transaction Pending',
                  'QI': 'Transaction incomplete - contact Westpac to confirm reconciliation',
                  'QR': 'Transaction Retry',
                  'QX': 'Network Error has occurred',
            ],
            '3': [
                  'Q3': 'Payment Gateway Connection Error',
                  'Q6': 'Duplicate Transaction � requery to determine status',
                  'QA': 'Invalid parameters or Initialisation failed',
                  'QB': 'Order type not currently supported',
                  'QC': 'Invalid Order Type',
                  'QE': 'Internal Error',
                  'QF': 'Transaction Failed',
                  'QG': 'Unknown Customer Order Number',
                  'QH': 'Unknown Customer Username or Password',
                  'QJ': 'Invalid Client Certificate',
                  'QK': 'Unknown Customer Merchant',
                  'QL': 'Business Group not configured for customer',
                  'QM': 'Payment Instrument not configured for customer',
                  'QO': 'Missing Payment Instrument',
                  'QP': 'Missing Supplier Account',
                  'QT': 'Invalid currency',
                  'QU': 'Unknown Customer IP Address',
            ],
    ]
}
