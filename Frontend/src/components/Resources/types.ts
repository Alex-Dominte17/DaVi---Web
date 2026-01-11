export interface Resource {
  id: string;
  type: 'Exercise' | 'Serious Game' | 'Medication' | 'Web Resource';
  difficulty?: 'easy' | 'medium' | 'hard';
  duration?: string;
  title: string;
  description: string;
  uri: string;
  tags: string[];
}