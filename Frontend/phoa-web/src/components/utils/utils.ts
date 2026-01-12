export const getCurrentSeason = () => {
  const now = new Date();
  const month = now.getMonth();

  if (month === 11 || month <= 1) return "Winter";
  if (month >= 2 && month <= 4) return "Spring";
  if (month >= 5 && month <= 7) return "Summer";
  return "Autumn";
};