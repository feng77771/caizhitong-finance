from flask import Flask
from flask import request
from flask_cors import CORS

import joblib
import numpy as np

app = Flask(__name__)
CORS(app)

print("Loading risk assessment model...")
try:
    model = joblib.load("risk_model.pkl")
    print("Model loaded successfully!")
except Exception as e:
    print(f"Model load failed: {e}")
    model = None

# 计算各指标的风险评分
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

def calculate_eps_score(eps):
    if eps > 1:
        return 0
    elif eps > 0.5:
        return 25
    elif eps > 0:
        return 50
    else:
        return 100

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

def calculate_cash_score(cash_flow):
    if cash_flow > 0:
        return 0
    else:
        return 70

def calculate_bps_score(bps):
    if bps > 5:
        return 0
    elif bps > 3:
        return 25
    elif bps > 1:
        return 50
    else:
        return 100

@app.post("/predict")
def predict():
    if model is None:
        return {"error": "Model not loaded"}, 500
    
    try:
        data = request.json
        
        eps = data["EPS"]
        roe = data["ROE"]
        gross_margin = data["gross_margin"]
        revenue_growth = data["revenue_growth"]
        profit_growth = data["profit_growth"]
        cash_flow = data["cash_flow"]
        bps = data["bps"]
        
        # 计算各指标的风险评分
        roe_score = calculate_roe_score(roe)
        eps_score = calculate_eps_score(eps)
        gross_score = calculate_gross_score(gross_margin)
        revenue_score = calculate_revenue_score(revenue_growth)
        profit_score = calculate_profit_score(profit_growth)
        cash_score = calculate_cash_score(cash_flow)
        bps_score = calculate_bps_score(bps)
        
        # 综合风险得分（加权计算，结果0-100分）
        # ROE: 25%, 利润增长率: 20%, 现金流: 20%, 营收增长率: 15%, 毛利率: 10%, EPS: 5%, BPS: 5%
        risk_score = (
            roe_score * 0.25 +
            profit_score * 0.20 +
            cash_score * 0.20 +
            revenue_score * 0.15 +
            gross_score * 0.10 +
            eps_score * 0.05 +
            bps_score * 0.05
        )
        risk_prob = round(float(risk_score), 2)
        
        X = np.array([[eps, roe, gross_margin, revenue_growth, profit_growth, cash_flow, bps]])
        model_prob = model.predict_proba(X)
        num_classes = model_prob.shape[1]
        
        # 使用模型预测结果进行修正
        if num_classes == 3:
            model_risk = round(float(model_prob[0][0] * 0 + model_prob[0][1] * 50 + model_prob[0][2] * 100), 2)
            # 综合评分和模型预测取平均
            risk_prob = round((risk_prob + model_risk) / 2, 2)
            print(f"预测概率分布(3类): 低风险={model_prob[0][0]:.4f}, 中风险={model_prob[0][1]:.4f}, 高风险={model_prob[0][2]:.4f}")
        elif num_classes == 2:
            model_risk = round(float(model_prob[0][0] * 0 + model_prob[0][1] * 75), 2)
            risk_prob = round((risk_prob + model_risk) / 2, 2)
            print(f"预测概率分布(2类): 类别0={model_prob[0][0]:.4f}, 类别1={model_prob[0][1]:.4f}")
        
        print(f"各指标风险评分 - ROE:{roe_score}, EPS:{eps_score}, 毛利率:{gross_score}, 营收增长:{revenue_score}, 利润增长:{profit_score}, 现金流:{cash_score}, BPS:{bps_score}, 综合:{risk_prob}%")
        
        # 根据风险概率确定风险等级（与概率一致）
        if risk_prob < 30:
            risk_level = "低风险"
        elif risk_prob < 60:
            risk_level = "中风险"
        else:
            risk_level = "高风险"

        return {
            "riskProb": risk_prob,
            "riskLevel": risk_level,
            "indicators": {
                "roe": {"score": roe_score, "weight": 25, "value": round(roe, 2)},
                "eps": {"score": eps_score, "weight": 5, "value": round(eps, 4)},
                "gross_margin": {"score": gross_score, "weight": 10, "value": round(gross_margin, 2)},
                "revenue_growth": {"score": revenue_score, "weight": 15, "value": round(revenue_growth, 2)},
                "profit_growth": {"score": profit_score, "weight": 20, "value": round(profit_growth, 2)},
                "cash_flow": {"score": cash_score, "weight": 20, "value": round(cash_flow, 6)},
                "bps": {"score": bps_score, "weight": 5, "value": round(bps, 2)}
            }
        }
    except Exception as e:
        print(f"Prediction error: {e}")
        return {"error": str(e)}, 400

if __name__ == "__main__":
    print("Starting risk assessment service on port 5005...")
    app.run(host="0.0.0.0", port=5005, debug=True)