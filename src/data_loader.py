import pandas as pd

DATA_PATH = "data/raw/employee_data.csv"


def load_data():
    df = pd.read_csv(DATA_PATH)
    return df


if __name__ == "__main__":
    df = load_data()

    print("Dataset loaded successfully!")
    print("Shape:", df.shape)

    print("\nColumns:")
    print(df.columns.tolist())

    print("\nFirst 5 rows:")
    print(df.head())