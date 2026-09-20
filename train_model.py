import pandas as pd
import numpy as np
import joblib
import pymysql
from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score, classification_report, confusion_matrix

from xgboost import XGBClassifier

print("正在连接数据库并读取2025年最新财务数据...")

# 连接数据库
conn = pymysql.connect(
    host="localhost",
    user="root",
    password="Fjy204716@",
    database="finance"
)

# 从数据库读取2025年最新财务指标数据
sql = """
SELECT 
    stock_code,
    stock_name,
    EPS, 
    ROE, 
    gross_margin, 
    revenue_growth, 
    profit_growth, 
    cash_flow, 
    bps 
FROM financial_indicators
WHERE report_date = '20251231'
"""

df = pd.read_sql(sql, conn)
conn.close()

print(f"成功读取 {len(df)} 条2025年最新财务数据")

# 根据财务指标计算风险分数（专业金融逻辑）

# 1. ROE评分 (0-100分, 权重25%)
def calculate_roe_score(roe):
    if roe > 15:
        return 0
    elif roe > 10:
        return 15
    elif roe > 5:
        return 30
    elif roe > 0:
        return 60
    else:
        return 100

# 2. EPS评分 (0-100分, 权重5%)
def calculate_eps_score(eps):
    if eps > 1:
        return 0
    elif eps > 0.5:
        return 25
    elif eps > 0:
        return 50
    else:
        return 100

# 3. 毛利率评分 (0-100分, 权重10%)
def calculate_gross_score(gross_margin):
    if gross_margin > 30:
        return 0
    elif gross_margin > 20:
        return 20
    elif gross_margin > 10:
        return 50
    elif gross_margin > 0:
        return 75
    else:
        return 100

# 4. 营收增长率评分 (0-100分, 权重15%)
def calculate_revenue_score(revenue_growth):
    if revenue_growth > 20:
        return 0
    elif revenue_growth > 10:
        return 20
    elif revenue_growth > 0:
        return 40
    elif revenue_growth > -10:
        return 70
    else:
        return 100

# 5. 利润增长率评分 (0-100分, 权重20%)
def calculate_profit_score(profit_growth):
    if profit_growth > 20:
        return 0
    elif profit_growth > 10:
        return 20
    elif profit_growth > 0:
        return 40
    elif profit_growth > -20:
        return 70
    else:
        return 100

# 6. 现金流评分 (0-100分, 权重20%)
def calculate_cash_score(cash_flow):
    if cash_flow > 0:
        return 0
    else:
        return 70

# 7. BPS评分 (0-100分, 权重5%)
def calculate_bps_score(bps):
    if bps > 5:
        return 0
    elif bps > 3:
        return 25
    elif bps > 1:
        return 50
    else:
        return 100

# 计算各指标得分
df['roe_score'] = df['ROE'].apply(calculate_roe_score)
df['eps_score'] = df['EPS'].apply(calculate_eps_score)
df['gross_score'] = df['gross_margin'].apply(calculate_gross_score)
df['revenue_score'] = df['revenue_growth'].apply(calculate_revenue_score)
df['profit_score'] = df['profit_growth'].apply(calculate_profit_score)
df['cash_score'] = df['cash_flow'].apply(calculate_cash_score)
df['bps_score'] = df['bps'].apply(calculate_bps_score)

# 综合风险得分（加权计算，结果0-100分）
# ROE: 25%, 利润增长率: 20%, 现金流: 20%, 营收增长率: 15%, 毛利率: 10%, EPS: 5%, BPS: 5%
df['risk_score'] = (
    df['roe_score'] * 0.25 +
    df['profit_score'] * 0.20 +
    df['cash_score'] * 0.20 +
    df['revenue_score'] * 0.15 +
    df['gross_score'] * 0.10 +
    df['eps_score'] * 0.05 +
    df['bps_score'] * 0.05
)

# 根据风险分数确定风险等级
# 0~30: 低风险, 30~60: 中风险, 60~100: 高风险
df['risk'] = np.where(
    df['risk_score'] < 30,
    0,  # 低风险
    np.where(
        df['risk_score'] < 60,
        1,  # 中风险
        2   # 高风险
    )
)

print("数据分布：")
print(df['risk'].value_counts())

X = df[[
    'EPS',
    'ROE',
    'gross_margin',
    'revenue_growth',
    'profit_growth',
    'cash_flow',
    'bps'
]]

y = df['risk']

# 划分训练集和测试集（80%训练，20%测试）
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42, stratify=y)

print(f"\n训练集样本数: {len(X_train)}, 测试集样本数: {len(X_test)}")
print("\n正在训练XGBoost模型...")

model = XGBClassifier(
    n_estimators=500,
    max_depth=8,
    learning_rate=0.03,
    random_state=42,
    subsample=0.8,
    colsample_bytree=0.8,
    gamma=0.1,
    reg_alpha=0.1,
    reg_lambda=1.0,
    objective='multi:softprob',
    num_class=3,
    eval_metric='mlogloss'
)

model.fit(X_train, y_train)

# 在训练集上预测
y_train_pred = model.predict(X_train)
train_accuracy = accuracy_score(y_train, y_train_pred)

# 在测试集上预测
y_test_pred = model.predict(X_test)
test_accuracy = accuracy_score(y_test, y_test_pred)

# 保存模型
joblib.dump(model, "risk_model.pkl")

print("\n" + "="*60)
print("模型评估结果（使用2025年最新财务数据）")
print("="*60)
print(f"训练集准确率: {train_accuracy * 100:.2f}%")
print(f"测试集准确率: {test_accuracy * 100:.2f}%")
print("\n分类报告:")
unique_classes = sorted(y.unique())
target_names = ['低风险', '中风险', '高风险']
selected_names = [target_names[i] for i in unique_classes]
print(classification_report(y_test, y_test_pred, labels=unique_classes, target_names=selected_names))
print("\n混淆矩阵:")
print(confusion_matrix(y_test, y_test_pred))
print("\n特征重要性：")
for name, importance in zip(X.columns, model.feature_importances_):
    print(f"  {name}: {importance:.4f}")

print("\n训练完成！模型已保存为 risk_model.pkl")
print("注意：预测时将以XGBoost模型预测结果为准，不使用数据库中的risk字段")