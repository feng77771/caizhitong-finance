const XLSX = require('xlsx');

const workbook = XLSX.readFile('D:\\财报\\financial_indicators.xlsx');
const sheetName = workbook.SheetNames[0];
const worksheet = workbook.Sheets[sheetName];
const data = XLSX.utils.sheet_to_json(worksheet);

if (data.length > 0) {
    console.log('所有列名:');
    console.log(Object.keys(data[0]));
    console.log('\n前3行完整数据:');
    console.log(JSON.stringify(data.slice(0, 3), null, 2));
}